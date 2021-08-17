package dev.fiki.forgehax.main.mods.player;

import com.google.common.collect.Lists;
import dev.fiki.forgehax.api.BlockHelper;
import dev.fiki.forgehax.api.BlockHelper.BlockTraceInfo;
import dev.fiki.forgehax.api.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.api.cmd.settings.DoubleSetting;
import dev.fiki.forgehax.api.cmd.settings.IntegerSetting;
import dev.fiki.forgehax.api.color.Colors;
import dev.fiki.forgehax.api.common.PriorityEnum;
import dev.fiki.forgehax.api.draw.GeometryMasks;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.entity.PlayerRotationEvent;
import dev.fiki.forgehax.api.events.render.RenderSpaceEvent;
import dev.fiki.forgehax.api.extension.*;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.api.reflection.ReflectionTools;
import dev.fiki.forgehax.main.services.SneakService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import lombok.val;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.BlockItem;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.opengl.GL11;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;

import static dev.fiki.forgehax.main.Common.*;
import static net.minecraft.network.play.client.CEntityActionPacket.Action;

@RegisterMod(
    name = "Scaffold",
    description = "Place blocks under yourself",
    category = Category.PLAYER
)
@RequiredArgsConstructor
@ExtensionMethod({GeneralEx.class, ItemEx.class, LocalPlayerEx.class, EntityEx.class, VectorEx.class, VertexBuilderEx.class})
public class Scaffold extends ToggleMod {
  private final SneakService sneaks;
  private final ReflectionTools reflection;

  private final EnumSet<Direction> horizontal = EnumSet.copyOf(Lists.newArrayList(Direction.Plane.HORIZONTAL));

  private final IntegerSetting delay = newIntegerSetting()
      .name("delay")
      .description("Block place delay")
      .defaultTo(4)
      .build();

  private final DoubleSetting placeDistance = newDoubleSetting()
      .name("place-distance")
      .description("Place block before side is fully visible. Use 0 to disable")
      .defaultTo(0.1d)
      .min(0.d)
      .build();

  private final BooleanSetting motionPrediction = newBooleanSetting()
      .name("motion-prediction")
      .description("Will try and predict the future position to place a block")
      .defaultTo(true)
      .build();

  private final BooleanSetting debug = newBooleanSetting()
      .name("debug")
      .description("Shows debug render info")
      .defaultTo(false)
      .build();

  private int tickCount = 0;
  private BlockPos currentTarget = null;
  private boolean predicted = false;

  @Override
  protected void onEnabled() {
    currentTarget = null;
    predicted = false;
    tickCount = 0;
  }

  @Override
  public String getDebugDisplayText() {
    return super.getDebugDisplayText()
        + " ["
        + "ticks = " + tickCount + ", "
        + "vel = " + (getLocalPlayer() == null
        ? "(0.00, 0.00)"
        : String.format("(%.2f, %.2f)", getLocalPlayer().getDeltaMovement().x(), getLocalPlayer().getDeltaMovement().z()))
        + ", "
        + "pos = " + (currentTarget == null
        ? "(0, 0, 0)"
        : String.format("(%d, %d, %d)", currentTarget.getX(), currentTarget.getY(), currentTarget.getZ()))
        + "]";
  }

  @SubscribeListener(priority = PriorityEnum.HIGH)
  public void onLocalPlayerMovementUpdate(PlayerRotationEvent event) {
    currentTarget = null;

    val lp = getLocalPlayer();
    Vector3d directionVector = Vector3d.ZERO;
    BlockPos below = lp.blockPosition().below();
    if (BlockHelper.isBlockReplaceable(below)) {
      currentTarget = below;
      predicted = true;
    } else if (motionPrediction.isEnabled()) {
      // try and get the block the player will be over
      Vector3d motion = lp.getDeltaMovement();
      // ignore y motion
      Vector3d vel = new Vector3d(motion.x(), 0.d, motion.z()).normalize();

      // must be moving
      if (vel.lengthSqr() > 0.d) {
        double modX, modZ;
        if (Math.abs(vel.x()) > Math.abs(vel.z())) {
          modX = vel.x() < 0.d ? -1.d : 1.d;
          modZ = 0.d;
        } else {
          modX = 0.d;
          modZ = vel.z() < 0.d ? -1.d : 1.d;
        }

        directionVector = new Vector3d(modX, 0.d, modZ);
        BlockPos forward = below.offset(directionVector.x(), directionVector.y(), directionVector.z());
        if (BlockHelper.isBlockReplaceable(forward)) {
          currentTarget = forward;
          predicted = true;
        }
      }
    }

    // no valid block found
    if (currentTarget == null) {
      return;
    }

    final Slot items = lp.getHotbarSlots().stream()
        .filter(Slot::hasItem)
        .filter(slot -> slot.getItem().getItem() instanceof BlockItem)
        .filter(slot -> slot.getItem().getBlockForItem().defaultBlockState().isCollisionShapeFullBlock(getWorld(), BlockPos.ZERO))
        .min(Comparator.comparingInt(ItemEx::getDistanceFromSelected))
        .orElse(null);

    if (items == null) {
      return;
    }

    boolean isPredictedTarget = directionVector.lengthSqr() > 0.d;

    final Vector3d realEyes = lp.getEyePos();
    final Vector3d eyes = isPredictedTarget
        ? realEyes.add(directionVector)
        : realEyes;
    final Vector3d dir = lp.getServerDirectionVector();

    BlockTraceInfo trace =
        Optional.ofNullable(BlockHelper.getPlaceableBlockSideTrace(eyes, dir, currentTarget))
            .filter(tr -> lp.canPlaceBlock(items.getItem().getBlockForItem(), tr.getPos()))
            .orElseGet(() -> horizontal.stream()
                .map(currentTarget::relative)
                .filter(BlockHelper::isBlockReplaceable)
                .map(bp -> BlockHelper.getPlaceableBlockSideTrace(eyes, dir, bp))
                .filter(Objects::nonNull)
                .filter(tr -> lp.canPlaceBlock(items.getItem().getBlockForItem(), tr.getPos()))
                .max(Comparator.comparing(BlockTraceInfo::isSneakRequired))
                .orElse(null));

    if (trace == null) {
      return;
    }

    currentTarget = trace.getPos();
    predicted = false;
    Vector3d hit = trace.getHitVec();
    event.setViewAngles(lp.getLookAngles(hit));

    // cannot place yet because of delay
    if (tickCount++ < delay.intValue()) {
      return;
    }

    // block is too far away or not visible
    if (placeDistance.doubleValue() > 0.d
        ? lp.position().distanceTo(trace.getCenterPos()) < placeDistance.doubleValue()
        : isPredictedTarget) {
      return;
    }

    final BlockTraceInfo tr = trace;
    event.onFocusGained(() -> {
      final Runnable resetSelected = lp.setSelectedSlot(items, t -> true);

      boolean sneak = tr.isSneakRequired() && !lp.isCrouchSneaking();
      if (sneak) {
        // send start sneaking packet
        getNetworkManager().dispatchSilentNetworkPacket(new CEntityActionPacket(lp, Action.PRESS_SHIFT_KEY));

        sneaks.setSneaking(true);
        sneaks.setSuppressing(true);
      }

      val blockTr = new BlockRayTraceResult(tr.getHitVec(), tr.getOppositeSide(), tr.getPos(), false);
      if (lp.placeBlock(Hand.MAIN_HAND, blockTr).consumesAction()) {
        lp.swingHandSilently();
      }

      if (sneak) {
        sneaks.setSneaking(false);
        sneaks.setSuppressing(false);

        getNetworkManager().dispatchNetworkPacket(new CEntityActionPacket(lp, Action.RELEASE_SHIFT_KEY));
      }

      resetSelected.run();

      reflection.Minecraft_rightClickDelay.set(MC, delay.getValue());
      tickCount = 0;
    });
  }

  @SubscribeListener
  public void onRender(RenderSpaceEvent event) {
    final BlockPos current = currentTarget;
    if (debug.isEnabled() && current != null) {
      val buffer = event.getBuffer();
      val stack = event.getStack();
      stack.pushPose();

      buffer.beginLines(DefaultVertexFormats.POSITION_COLOR);
      stack.translateVec(event.getProjectedPos().scale(-1));

      buffer.outlinedCube(current, current.offset(1.D, 1.D, 1.D), GeometryMasks.Line.ALL,
          predicted ? Colors.YELLOW : Colors.ORANGE, stack.getLastMatrix());

      GL11.glEnable(GL11.GL_LINE_SMOOTH);
      buffer.draw();
      GL11.glDisable(GL11.GL_LINE_SMOOTH);

      stack.popPose();
    }
  }
}
