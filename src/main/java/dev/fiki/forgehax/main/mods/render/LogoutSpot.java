package dev.fiki.forgehax.main.mods.render;

import com.google.common.collect.Sets;
import dev.fiki.forgehax.api.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.api.cmd.settings.IntegerSetting;
import dev.fiki.forgehax.api.color.Colors;
import dev.fiki.forgehax.api.draw.GeometryMasks;
import dev.fiki.forgehax.api.draw.SurfaceHelper;
import dev.fiki.forgehax.api.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.api.events.PlayerConnectEvent;
import dev.fiki.forgehax.api.events.Render2DEvent;
import dev.fiki.forgehax.api.events.RenderEvent;
import dev.fiki.forgehax.api.extension.VectorEx;
import dev.fiki.forgehax.api.extension.VertexBuilderEx;
import dev.fiki.forgehax.api.math.ScreenPos;
import dev.fiki.forgehax.api.math.VectorUtil;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.ExtensionMethod;
import lombok.val;
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
@ExtensionMethod({VectorEx.class, VertexBuilderEx.class})
public class LogoutSpot extends ToggleMod {
  private final BooleanSetting render = newBooleanSetting()
      .name("render")
      .description("Draw a box where the player logged out")
      .defaultTo(true)
      .build();

  private final IntegerSetting maxDistance = newIntegerSetting()
      .name("max-distance")
      .description("Distance from box before deleting it")
      .defaultTo(320)
      .build();

  private final BooleanSetting printMessage = newBooleanSetting()
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
    if (printMessage.getValue()) {
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
        if (spots.add(new LogoutPos(
            event.getPlayerInfo().getUuid(),
            event.getPlayerInfo().getName(),
            bb.getMaxs(), bb.getMins()))) {
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
        ScreenPos upper = VectorUtil.toScreen(top);
        if (upper.isVisible()) {
          double distance = getLocalPlayer().getPositionVec().distanceTo(top);
          String name = String.format("%s (%.1f)", spot.getName(), distance);
          SurfaceHelper.drawTextShadow(
              name,
              (float) (upper.getX() - (SurfaceHelper.getStringWidth(name) / 2.f)),
              (float) (upper.getY() - (SurfaceHelper.getStringHeight() + 1.f)),
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

    val stack = event.getMatrixStack();
    val builder = event.getBuffer();
    stack.push();

    builder.beginLines(DefaultVertexFormats.POSITION_COLOR);

    synchronized (spots) {
      for (LogoutPos spot : spots) {
        builder.outlinedCube(spot.getMins(), spot.getMaxs(),
            GeometryMasks.Line.ALL, Colors.RED, stack.getLastMatrix());
      }
    }

    builder.draw();
    stack.pop();
  }

  @SubscribeEvent
  public void onPlayerUpdate(LocalPlayerUpdateEvent event) {
    if (maxDistance.getValue() > 0) {
      synchronized (spots) {
        spots.removeIf(pos -> getLocalPlayer().getPositionVec().distanceTo(pos.getTopVec())
            > maxDistance.getValue());
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

  @Getter
  @AllArgsConstructor
  private static class LogoutPos {
    final UUID id;
    final String name;
    final Vector3d maxs;
    final Vector3d mins;

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
