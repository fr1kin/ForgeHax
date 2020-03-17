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

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;


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


  private final List<Trail> trails = new ArrayList<>();

  private Anchor rootAnchor;
  private Anchor newestAnchor;

  private Set<Anchor> visibleLastTick = Collections.emptySet();
  private int dimension;

  private Supplier<Stream<Stream<Vec3d>>> pointsToDraw = Stream::empty; // dont want to compute this in the render event

  // this should be immutable
  private static class Trail {
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

    this.pointsToDraw = getPointsToDraw();

    this.visibleLastTick = getVisibleAnchors(this.rootAnchor); // should probably just reuse the list we just made
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
    final Stream<List<Anchor>> oldPaths = this.trails.stream()
        .filter(t -> t.dimension == this.dimension)
        .map(t -> t.path);

    // TODO: only pathfind when there is a new anchor
    // Trails<Anchors<Points>>>
    final List<List<List<Vec3d>>> trails =
        Stream.concat(
          oldPaths,
          Stream.of(pathFind(this.rootAnchor, this.newestAnchor))
        )
        .map(this::getAllPoints)
        .collect(Collectors.toList());

    return () -> trails.stream()
        .map(anchors -> anchors.stream().flatMap(List::stream));
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
    // This should never happen
    if (this.rootAnchor == null) return;

    this.trails.add(new Trail(this.dimension, this.rootAnchor, this.newestAnchor));
    this.rootAnchor = null;
    this.visibleLastTick = Collections.emptySet();
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
            this.pointsToDraw = Stream::empty;
            this.visibleLastTick = Collections.emptySet();
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
          Helper.printWarning("unimplemented");
        })
        .build();

    getCommandStub()
        .builders()
        .newCommandBuilder()
        .name("load")
        .description("Load a breadcrumb file")
        .requiredArgs(1)
        .processor(data -> {
          Helper.printWarning("unimplemented");
        })
        .build();
  }

}
