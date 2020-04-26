package com.matt.forgehax.mods;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.matt.forgehax.Helper;
import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.events.RenderEvent;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.matt.forgehax.Helper.getFileManager;


@RegisterMod
public class BreadCrumbs extends ToggleMod {

  public final Setting<Integer> smoothness =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("smoothness")
          .description("rendering smoothness")
          .defaultTo(1)
          .min(1)
          .build();

  /*public final Setting<Integer> maxpoints =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("maxpoints")
          .description("maximum number of points to save (0 for no limit)")
          .defaultTo(1)
          .min(1)
          .build();*/

  /*public final Setting<Boolean> simplify =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("simplify")
          .description("Simplify the path")
          .defaultTo(false)
          .build();*/

  public final Setting<Boolean> drawIntermediate =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("intermediate") // TODO: give this a better name
          .description("Draw points between anchors")
          .defaultTo(true)
          .build();

  public BreadCrumbs() {
    super(Category.RENDER, "BreadCrumbs", false, "epic trail meme");
  }

  private static final Path BASE_PATH = getFileManager().getBaseResolve("breadcrumbs");

  private List<Trail> trails = new ArrayList<>();
  private boolean recording = true; // TODO: use this

  private Anchor rootAnchor;
  private Anchor newestAnchor;

  private Set<Anchor> visibleLastTick = Collections.emptySet();
  private int dimension;

  private Supplier<Stream<Stream<Vec3d>>> pointsToDraw = Stream::empty; // dont want to compute this in the render event

  // this should be immutable
  /*private static class Trail {
    final int dimension;
    private final Anchor root;
    private final Anchor last;
    final List<Anchor> path;

    Trail(int dim, Anchor root, Anchor last) {
      this.dimension = dim;
      this.root = root;
      this.last = last;
      this.path = Collections.unmodifiableList(pathFind(root, last));
    }
  }*/
  private static class Trail {
    final int dimension;
    final List<Vec3d> points;

    Trail(int dim, List<Vec3d> path) {
      this.dimension = dim;
      this.points = Collections.unmodifiableList(path);
    }

    static Trail fromGraph(int dim, Anchor root, Anchor target, BreadCrumbs bc) {
      final List<Anchor> anchors = pathFind(root, target);
      final List<List<Vec3d>> points = bc.getAllPoints(anchors);
      final List<Vec3d> flat = new ArrayList<>();
      points.forEach(flat::addAll);
      return new Trail(dim, flat);
    }
  }

  private static class Anchor {
    final Vec3d pos;
    final List<Vec3d> points;
    final List<Anchor> connected;

    Anchor(Vec3d pos) {
      this(pos, new ArrayList<>(), new ArrayList<>());
    }

    Anchor(Vec3d pos, List<Vec3d> points, List<Anchor> connected) {
      this.pos = pos;
      this.points = points;
      this.connected = connected;
    }

    void connectAnchor(Anchor anchor) {
      this.connected.add(anchor);
    }
  }

  /*private static class AnchorGraph {
    final Anchor root;
    final Set<Anchor> vertices = new HashSet<>();
  }*/

  private enum Serialization {;
    static {
      try {
        if (!Files.exists(BASE_PATH)) Files.createDirectories(BASE_PATH);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    private static Trail readTrail(DataInputStream dis) throws IOException {
      final int dim = dis.readInt();
      final int size = dis.readInt(); // number of points
      final List<Vec3d> points = new ArrayList<>(size);
      for (int i = 0; i < size; i++) {
        points.add(new Vec3d(
           dis.readDouble(),
           dis.readDouble(),
           dis.readDouble()
        ));
      }

      return new Trail(dim, points);
    }

    private static void writeTrail(Trail trail, DataOutputStream dos) throws IOException {
      dos.writeInt(trail.dimension);
      dos.writeInt(trail.points.size());
      for (Vec3d p : trail.points) {
        dos.writeDouble(p.x);
        dos.writeDouble(p.y);
        dos.writeDouble(p.z);
      }
    }

    static List<Trail> deserialize(Path p) throws IOException {
      try (DataInputStream is = new DataInputStream(Files.newInputStream(p))) {
        final int numTrails = is.readInt();
        final List<Trail> trails = new ArrayList<>(numTrails);
        for (int i = 0; i < numTrails; i++) {
          trails.add(readTrail(is));
        }

        return trails;
      }
    }

    static void serialize(List<Trail> trails, Path p) throws IOException {
      try (DataOutputStream dos = new DataOutputStream(Files.newOutputStream(p))) {
        dos.writeInt(trails.size());
        for (Trail t : trails) {
          writeTrail(t, dos);
        }
      }
    }
  }

  private static boolean isVisible(Anchor anchor) {
    if (!MC.world.isAreaLoaded(new BlockPos(anchor.pos), 1, false)) {
      return false;
    } else {
      Vec3d from = new Vec3d(MC.player.posX, MC.player.posY, MC.player.posZ);
      RayTraceResult result = MC.world.rayTraceBlocks(from, anchor.pos, false, true, true);
      return result == null || result.typeOfHit == RayTraceResult.Type.MISS;
    }
  }

  private static void forEachAnchor(Anchor root, Consumer<Anchor> fn) {
    forEachAnchor0(root, new HashSet<>(), fn);
  }

  private static Set<Anchor> getAllAnchors(Anchor root) {
    final Set<Anchor> visited = new HashSet<>();
    forEachAnchor0(root, visited, (unused) -> {});
    return visited;
  }

  private static void forEachAnchor0(Anchor root, Set<Anchor> visited, Consumer<Anchor> fn) {
    if (visited.contains(root)) return;
    visited.add(root);
    fn.accept(root);
    for (Anchor anchor : root.connected) {
      forEachAnchor0(anchor, visited, fn);
    }
  }

  /*private static Anchor minAnchor(Anchor root, Comparator<Anchor> comparator) {
    return minAnchor(root, root, comparator);
  }

  private static Anchor minAnchor(Anchor root, Anchor min, Comparator<Anchor> comparator) {
    if (comparator.compare(root, min) < 0) { // TODO: make sure < 0 is correct
      min = root;
    }
    for (Anchor child : root.connected) {
      min = minAnchor(child, min, comparator);
    }
    return min;
  }*/

  private static Set<Anchor> getVisibleAnchors(Anchor root) {
    final Set<Anchor> all = getAllAnchors(root);
    all.removeIf(anchor -> !isVisible(anchor));
    return all;
  }


  private static <T> List<T> listDifference(List<T> a, List<T> b) {
    final List<T> out = new ArrayList<>(a.size());
    for (T x : a) {
      if (!b.contains(x)) out.add(x);
    }
    return out;
  }


  private static List<Anchor> getPath(Anchor source, Anchor target, Map<Anchor, Anchor> prev) {
    final List<Anchor> out = new ArrayList<>();
    Anchor it = target;
    if (prev.containsKey(it) || it == source) {
      while (it != null) {
        out.add(it); // supposed to add to the beginning of the list but returning a reverse view will have the same effect
        it = prev.get(it);
      }
    }
    return Lists.reverse(out);
  }
  private static double length(Anchor a, Anchor b) {
    return a.pos.distanceTo(b.pos);
  }
  private static List<Anchor> pathFind(Anchor root, Anchor target) {
    final Set<Anchor> unvisited = getAllAnchors(root);
    final Map<Anchor, Double> distances = new HashMap<>();
    for (Anchor it : unvisited) distances.put(it, Double.POSITIVE_INFINITY);
    distances.put(root, 0.D);
    final Map<Anchor, Anchor> prev = new HashMap<>();

    while (!unvisited.isEmpty()) {
      final Anchor u = Collections.min(unvisited, Comparator.comparingDouble(distances::get));
      unvisited.remove(u);
      if (u == target) break;

      for (Anchor v : u.connected) {
        final double alt = distances.get(u) + length(u, v);
        if (alt < distances.get(v)) {
          distances.put(v, alt);
          prev.put(v, u);
        }
      }
    }

    return getPath(root, target, prev);
  }

  private static <T> Optional<T> getLast(List<T> list) {
    return !list.isEmpty() ? Optional.of(list.get(list.size() - 1)) : Optional.empty();
  }

  @SubscribeEvent
  public void onPlayerUpdate(LocalPlayerUpdateEvent event) {
    if (Helper.getModManager().get(FreecamMod.class).map(ToggleMod::isEnabled).orElse(false)) return;

    if (this.recording) {
      final Vec3d playerPos = new Vec3d(MC.player.posX, MC.player.posY, MC.player.posZ);

      // first tick of a trail
      if (this.rootAnchor == null) {
        this.rootAnchor = new Anchor(playerPos);
        this.newestAnchor = rootAnchor;
        this.dimension = MC.player.dimension;
        return;
      }

      final Set<Anchor> visibleAnchors = getVisibleAnchors(this.rootAnchor);
      final Set<Anchor> noLongerVisible = Sets.difference(this.visibleLastTick, visibleAnchors);

      final Set<Anchor> allOldAnchors = Sets.difference(getAllAnchors(rootAnchor), Collections.singleton(this.newestAnchor));

      final Optional<Anchor> closest = !allOldAnchors.isEmpty() ? // TODO: make this a small list?
          Optional.of(Collections.min(allOldAnchors, Comparator.comparingDouble(anch -> anch.pos.distanceTo(playerPos))))
          : Optional.empty();

      if (noLongerVisible.contains(this.newestAnchor) || closest.map(noLongerVisible::contains).orElse(false)) { // new anchor
        final Anchor newAnchor = new Anchor(playerPos);
        Stream.of(noLongerVisible, visibleAnchors)
            .flatMap(Set::stream)
            .distinct()
            .forEach(anch -> anch.connectAnchor(newAnchor));
        this.newestAnchor = newAnchor;
      } else { // new point for the last anchor
        final List<Vec3d> points = this.newestAnchor.points;
        final Optional<Vec3d> prev = getLast(points);
        // don't spam points if we don't move
        if (!prev.isPresent() || prev.get().distanceTo(playerPos) > 0.01) {
          points.add(playerPos);
        }
      }

      this.visibleLastTick = getVisibleAnchors(this.rootAnchor); // should probably just reuse the list we just made
    }

    this.pointsToDraw = getPointsToDraw();
  }

  private static <T> List<T> partialList(List<T> list, int smoothness) {
    if (smoothness == 1) return list;
    // TODO: return view of list
    final List<T> out = new ArrayList<>((int)Math.ceil(list.size() / (double)smoothness));
    for (int i = 0; i < list.size(); i += smoothness)  {
      out.add(list.get(i));
    }
    return out;
  }

  private List<List<Vec3d>> getAllPoints(List<Anchor> path) {
    final List<List<Vec3d>> points = new ArrayList<>();

    for (int i = 0; i < path.size(); i++) {
      final Anchor anchor = path.get(i);

      points.add(Collections.singletonList(anchor.pos)); // might be better to only use the anchor's list of points
      if (drawIntermediate.get()) {
        final int idx = i;
        // if this is the last anchor or this anchor's point list can be linked to the next
        if ((i == path.size() - 1) || getLast(anchor.points).map(p -> p.distanceTo(path.get(idx + 1).pos) < 1).orElse(false)) {
          points.add(partialList(anchor.points, this.smoothness.get()));
        }
      }
    }

    return points;
  }

  private Supplier<Stream<Stream<Vec3d>>> getPointsToDraw() {
    final List<List<Vec3d>> oldPaths = this.trails.stream()
        .filter(t -> t.dimension == this.dimension)
        .map(t -> t.points)
        .collect(Collectors.toList());

    // TODO: only pathfind when there is a new anchor
    // Trails<Anchors<Points>>>
    /*final List<List<List<Vec3d>>> trails =
        Stream.concat(
          oldPaths,
          Stream.of(pathFind(this.rootAnchor, this.newestAnchor))
        )
        .map(this::getAllPoints)
        .collect(Collectors.toList());*/
    final List<List<Vec3d>> currentTrail = this.rootAnchor != null ?
        getAllPoints(pathFind(this.rootAnchor, this.newestAnchor))
        : Collections.emptyList();

    /*return () -> trails.stream()
        .map(anchors -> anchors.stream().flatMap(List::stream));*/
    return () -> Stream.concat(
        oldPaths.stream().map(List::stream),
        Stream.of(currentTrail.stream().flatMap(List::stream))
    );
  }

  private void pushNewTrailAndReset() {
    this.trails.add(Trail.fromGraph(this.dimension, this.rootAnchor, this.newestAnchor, this));

    this.rootAnchor = null;
    this.newestAnchor = null;
    this.visibleLastTick = Collections.emptySet();
  }

  @SubscribeEvent
  public void onRender(RenderEvent event) {
    BufferBuilder builder = event.getBuffer();

    this.pointsToDraw.get().forEach(vecStream -> {
      final List<Vec3d> path = vecStream.collect(Collectors.toList()); // TODO: dont collect

      builder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
      path.forEach(p -> {
        builder.pos(p.x, p.y, p.z).color(255, 0, 0, 255).endVertex();
      });
      event.getTessellator().draw();
    });
  }


  @SubscribeEvent
  public void onPacketReceived(PacketEvent.Incoming.Pre event) {
    if (!(event.getPacket() instanceof SPacketRespawn)) return;
    MC.addScheduledTask(() -> {
      // This should never happen
      if (this.rootAnchor == null) return;

      this.pushNewTrailAndReset();
    });
  }

  @Override
  public void onLoad() {
    getCommandStub()
        .builders()
        .newCommandBuilder()
        .name("clear")
        .description("clear points")
        .processor(data -> {
          MC.addScheduledTask(() -> {
            this.rootAnchor = null;
            this.newestAnchor = null;
            this.visibleLastTick = Collections.emptySet();
            this.trails.clear();
            this.pointsToDraw = Stream::empty;
          });
        })
        .build();

    getCommandStub()
        .builders()
        .newCommandBuilder()
        .name("save")
        .description("Save breadcrumb history to file")
        .requiredArgs(1)
        .processor(data -> {
          MC.addScheduledTask(() -> {
            try {
              List<Trail> trails = new ArrayList<>(this.trails);
              if (this.rootAnchor != null) {
                trails.add(Trail.fromGraph(this.dimension, this.rootAnchor, this.newestAnchor, this));
              }

              final Path out = BASE_PATH.resolve(data.getArgumentAsString(0));
              Serialization.serialize(trails, out);
            } catch (IOException ex) {
              Helper.printError(ex.toString());
              ex.printStackTrace();
            }
          });
        })
        .build();

    getCommandStub()
        .builders()
        .newCommandBuilder()
        .name("load")
        .description("Load a breadcrumb file")
        .requiredArgs(1)
        .processor(data -> {
          MC.addScheduledTask(() -> {
            try {
              this.trails.clear();
              this.rootAnchor = null;
              this.newestAnchor = null;
              this.pointsToDraw = Stream::empty;

              this.trails.addAll(Serialization.deserialize(BASE_PATH.resolve(data.getArgumentAsString(0))));
              this.recording = false;
            } catch (IOException ex) {
              Helper.printError(ex.toString());
              ex.printStackTrace();
            }
          });
        })
        .build();

    getCommandStub()
        .builders()
        .newCommandBuilder()
        .name("pause")
        .description("Stop recording points")
        .processor(data -> {
          MC.addScheduledTask(() -> {
            this.recording = false;
            this.pushNewTrailAndReset();
          });
        })
        .build();
    getCommandStub()
        .builders()
        .newCommandBuilder()
        .name("resume")
        .description("Resume recording points")
        .processor(data -> {
          MC.addScheduledTask(() -> {
            this.recording = true;
          });
        })
        .build();
  }

}
