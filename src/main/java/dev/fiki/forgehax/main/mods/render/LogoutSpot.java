package dev.fiki.forgehax.main.mods.render;

import com.google.common.collect.Sets;
import dev.fiki.forgehax.main.util.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.main.util.cmd.settings.IntegerSetting;
import dev.fiki.forgehax.main.util.color.Colors;
import dev.fiki.forgehax.main.util.draw.BufferBuilderEx;
import dev.fiki.forgehax.main.util.draw.GeometryMasks;
import dev.fiki.forgehax.main.util.draw.SurfaceHelper;
import dev.fiki.forgehax.main.util.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.events.PlayerConnectEvent;
import dev.fiki.forgehax.main.util.events.Render2DEvent;
import dev.fiki.forgehax.main.util.events.RenderEvent;
import dev.fiki.forgehax.main.util.math.ScreenPos;
import dev.fiki.forgehax.main.util.math.VectorUtils;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Set;
import java.util.UUID;

import static dev.fiki.forgehax.main.Common.*;

@RegisterMod(
    name = "LogoutSpot",
    description = "Show where a player logs out",
    category = Category.RENDER
)
public class LogoutSpot extends ToggleMod {
  private final BooleanSetting render = newBooleanSetting()
      .name("render")
      .description("Draw a box where the player logged out")
      .defaultTo(true)
      .build();

  private final IntegerSetting max_distance = newIntegerSetting()
      .name("max-distance")
      .description("Distance from box before deleting it")
      .defaultTo(320)
      .build();

  private final BooleanSetting print_message = newBooleanSetting()
      .name("print-message")
      .description("Print connect/disconnect messages in chat")
      .defaultTo(true)
      .build();

  {
    newSimpleCommand()
        .name("clear")
        .description("Clear cloned players")
        .executor(args -> reset())
        .build();
  }

  private final Set<LogoutPos> spots = Sets.newHashSet();

  private void reset() {
    synchronized (spots) {
      spots.clear();
    }
  }

  private void printWarning(String fmt, Object... args) {
    if (print_message.getValue()) {
      printWarning(fmt, args);
    }
  }

  @Override
  protected void onDisabled() {
    reset();
  }

  @SubscribeEvent
  public void onPlayerConnect(PlayerConnectEvent.Join event) {
    synchronized (spots) {
      if (spots.removeIf(spot -> spot.getId().equals(event.getPlayerInfo().getUuid()))) {
        printWarning("%s has joined!", event.getPlayerInfo().getName());
      }
    }
  }

  @SubscribeEvent
  public void onPlayerDisconnect(PlayerConnectEvent.Leave event) {
    if (!isInWorld()) {
      return;
    }

    PlayerEntity player = getWorld().getPlayerByUuid(event.getPlayerInfo().getUuid());
    if (player != null && getLocalPlayer() != null && !getLocalPlayer().equals(player)) {
      AxisAlignedBB bb = player.getBoundingBox();
      synchronized (spots) {
        if (spots.add(
            new LogoutPos(
                event.getPlayerInfo().getUuid(),
                event.getPlayerInfo().getName(),
                new Vector3d(bb.maxX, bb.maxY, bb.maxZ),
                new Vector3d(bb.minX, bb.minY, bb.minZ)))) {
          printWarning("%s has disconnected!", event.getPlayerInfo().getName());
        }
      }
    }
  }

  @SubscribeEvent(priority = EventPriority.LOW)
  public void onRenderGameOverlayEvent(Render2DEvent event) {
    if (!render.getValue()) {
      return;
    }

    synchronized (spots) {
      spots.forEach(spot -> {
        Vector3d top = spot.getTopVec();
        ScreenPos upper = VectorUtils.toScreen(top);
        if (upper.isVisible()) {
          double distance = getLocalPlayer().getPositionVec().distanceTo(top);
          String name = String.format("%s (%.1f)", spot.getName(), distance);
          SurfaceHelper.drawTextShadow(
              name,
              (float)(upper.getX() - (SurfaceHelper.getStringWidth(name) / 2.f)),
              (float)(upper.getY() - (SurfaceHelper.getStringHeight() + 1.f)),
              Colors.RED.toBuffer());
        }
      });
    }
  }

  @SubscribeEvent
  public void onRender(RenderEvent event) {
    if (!render.getValue()) {
      return;
    }

    BufferBuilderEx builder = event.getBuffer();
    builder.beginLines(DefaultVertexFormats.POSITION_COLOR);

    synchronized (spots) {
      spots.forEach(spot -> builder.putOutlinedCuboid(spot.getMins(), spot.getMaxs(),
          GeometryMasks.Line.ALL, Colors.RED));
    }

    builder.draw();
  }

  @SubscribeEvent
  public void onPlayerUpdate(LocalPlayerUpdateEvent event) {
    if (max_distance.getValue() > 0) {
      synchronized (spots) {
        spots.removeIf(pos -> getLocalPlayer().getPositionVec().distanceTo(pos.getTopVec())
            > max_distance.getValue());
      }
    }
  }

  @SubscribeEvent
  public void onWorldUnload(WorldEvent.Unload event) {
    reset();
  }

  @SubscribeEvent
  public void onWorldLoad(WorldEvent.Load event) {
    reset();
  }

  private static class LogoutPos {
    final UUID id;
    final String name;
    final Vector3d maxs;
    final Vector3d mins;

    private LogoutPos(UUID uuid, String name, Vector3d maxs, Vector3d mins) {
      this.id = uuid;
      this.name = name;
      this.maxs = maxs;
      this.mins = mins;
    }

    public UUID getId() {
      return id;
    }

    public String getName() {
      return name;
    }

    public Vector3d getMaxs() {
      return maxs;
    }

    public Vector3d getMins() {
      return mins;
    }

    public Vector3d getTopVec() {
      return new Vector3d(
          (getMins().x + getMaxs().x) / 2.D, getMaxs().y, (getMins().z + getMaxs().z) / 2.D);
    }

    @Override
    public boolean equals(Object other) {
      return this == other
          || (other instanceof LogoutPos && getId().equals(((LogoutPos) other).getId()));
    }

    @Override
    public int hashCode() {
      return getId().hashCode();
    }
  }
}
