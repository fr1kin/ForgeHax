package dev.fiki.forgehax.main.mods.player;

import com.google.common.collect.Lists;
import dev.fiki.forgehax.main.managers.RotationManager;
import dev.fiki.forgehax.main.managers.RotationManager.RotationState.Local;
import dev.fiki.forgehax.main.services.HotbarSelectionService.ResetFunction;
import dev.fiki.forgehax.main.services.SneakService;
import dev.fiki.forgehax.main.util.BlockHelper;
import dev.fiki.forgehax.main.util.BlockHelper.BlockTraceInfo;
import dev.fiki.forgehax.main.util.PacketHelper;
import dev.fiki.forgehax.main.util.Utils;
import dev.fiki.forgehax.main.util.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.main.util.cmd.settings.DoubleSetting;
import dev.fiki.forgehax.main.util.cmd.settings.IntegerSetting;
import dev.fiki.forgehax.main.util.color.Colors;
import dev.fiki.forgehax.main.util.common.PriorityEnum;
import dev.fiki.forgehax.main.util.draw.BufferBuilderEx;
import dev.fiki.forgehax.main.util.draw.GeometryMasks;
import dev.fiki.forgehax.main.util.entity.LocalPlayerInventory;
import dev.fiki.forgehax.main.util.entity.LocalPlayerUtils;
import dev.fiki.forgehax.main.util.events.RenderEvent;
import dev.fiki.forgehax.main.util.math.VectorUtils;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import dev.fiki.forgehax.main.util.reflection.ReflectionTools;
import lombok.RequiredArgsConstructor;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.BlockItem;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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
public class Scaffold extends ToggleMod implements RotationManager.MovementUpdateListener {
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
    RotationManager.getManager().register(this, PriorityEnum.HIGHEST);
    currentTarget = null;
    predicted = false;
    tickCount = 0;
  }

  @Override
  protected void onDisabled() {
    RotationManager.getManager().unregister(this);
  }

  @Override
  public String getDebugDisplayText() {
    return super.getDebugDisplayText()
        + " ["
        + "ticks = " + tickCount + ", "
        + "vel = " + (getLocalPlayer() == null
          ? "(0.00, 0.00)"
          : String.format("(%.2f, %.2f)", getLocalPlayer().getMotion().getX(), getLocalPlayer().getMotion().getZ()))
        + ", "
        + "pos = " + (currentTarget == null
          ? "(0, 0, 0)"
          : String.format("(%d, %d, %d)", currentTarget.getX(), currentTarget.getY(), currentTarget.getZ()))
        + "]";
  }

  @Override
  public void onLocalPlayerMovementUpdate(Local state) {
    currentTarget = null;

    Vector3d directionVector = Vector3d.ZERO;
    BlockPos below = getLocalPlayer().getPosition().down();
    if(BlockHelper.isBlockReplaceable(below)) {
      currentTarget = below;
      predicted = true;
    } else if(motionPrediction.isEnabled()) {
      // try and get the block the player will be over
      Vector3d motion = getLocalPlayer().getMotion();
      // ignore y motion
      Vector3d vel = new Vector3d(motion.getX(), 0.d, motion.getZ()).normalize();

      // must be moving
      if(vel.lengthSquared() > 0.d) {
        double modX, modZ;
        if(Math.abs(vel.getX()) > Math.abs(vel.getZ())) {
          modX = vel.getX() < 0.d ? -1.d : 1.d;
          modZ = 0.d;
        } else {
          modX = 0.d;
          modZ = vel.getZ() < 0.d ? -1.d : 1.d;
        }

        directionVector = new Vector3d(modX, 0.d, modZ);
        BlockPos forward = below.add(directionVector.getX(), directionVector.getY(), directionVector.getZ());
        if(BlockHelper.isBlockReplaceable(forward)) {
          currentTarget = forward;
          predicted = true;
        }
      }
    }

    // no valid block found
    if(currentTarget == null) {
      return;
    }

    LocalPlayerInventory.InvItem items =
        LocalPlayerInventory.getHotbarInventory()
            .stream()
            .filter(LocalPlayerInventory.InvItem::nonNull)
            .filter(item -> item.getItem() instanceof BlockItem)
            .filter(item -> Block.getBlockFromItem(item.getItem()).getDefaultState().isOpaqueCube(getWorld(), BlockPos.ZERO))
            .max(Comparator.comparingInt(LocalPlayerInventory::getHotbarDistance))
            .orElse(LocalPlayerInventory.InvItem.EMPTY);

    if (items.isNull()) {
      return;
    }

    boolean isPredictedTarget = directionVector.lengthSquared() > 0.d;

    final Vector3d realEyes = getLocalPlayer().getEyePosition(1.f);
    final Vector3d eyes = isPredictedTarget
        ? realEyes.add(directionVector)
        : realEyes;
    final Vector3d dir = LocalPlayerUtils.getServerDirectionVector();

    BlockTraceInfo trace =
        Optional.ofNullable(BlockHelper.getPlaceableBlockSideTrace(eyes, dir, currentTarget))
            .filter(tr -> tr.isPlaceable(items))
            .orElseGet(() -> horizontal.stream()
                .map(currentTarget::offset)
                .filter(BlockHelper::isBlockReplaceable)
                .map(bp -> BlockHelper.getPlaceableBlockSideTrace(eyes, dir, bp))
                .filter(Objects::nonNull)
                .filter(tr -> tr.isPlaceable(items))
                .max(Comparator.comparing(BlockTraceInfo::isSneakRequired))
                .orElse(null));

    if (trace == null) {
      return;
    }

    currentTarget = trace.getPos();
    predicted = false;
    Vector3d hit = trace.getHitVec();
    state.setServerAngles(Utils.getLookAtAngles(hit));

    // cannot place yet because of delay
    if(tickCount++ < delay.intValue()) {
      return;
    }

    // block is too far away or not visible
    if(placeDistance.doubleValue() > 0.d
        ? getLocalPlayer().getPositionVec().distanceTo(trace.getCenterPos()) < placeDistance.doubleValue()
        : isPredictedTarget) {
      return;
    }

    final BlockTraceInfo tr = trace;
    state.invokeLater(rs -> {
      ResetFunction func = LocalPlayerInventory.setSelected(items);

      boolean sneak = tr.isSneakRequired() && !LocalPlayerUtils.isSneaking();
      if (sneak) {
        // send start sneaking packet
        PacketHelper.ignoreAndSend(
            new CEntityActionPacket(getLocalPlayer(), Action.PRESS_SHIFT_KEY));

        sneaks.setSneaking(true);
        sneaks.setSuppressing(true);
      }

      if (getPlayerController()
          .func_217292_a(
              getLocalPlayer(),
              getWorld(),
              Hand.MAIN_HAND,
              new BlockRayTraceResult(tr.getHitVec(), tr.getOppositeSide(), tr.getPos(), false))
          .isSuccessOrConsume()) {
        sendNetworkPacket(new CAnimateHandPacket(Hand.MAIN_HAND));
      }

      if (sneak) {
        sneaks.setSneaking(false);
        sneaks.setSuppressing(false);

        sendNetworkPacket(new CEntityActionPacket(getLocalPlayer(), Action.RELEASE_SHIFT_KEY));
      }

      func.revert();

      reflection.Minecraft_rightClickDelayTimer.set(MC, delay.getValue());
      tickCount = 0;
    });
  }

  @SubscribeEvent
  public void onRender(RenderEvent event) {
    final BlockPos current = currentTarget;
    if(debug.isEnabled() && current != null) {
      BufferBuilderEx buffer = event.getBuffer();

      buffer.beginLines(DefaultVertexFormats.POSITION_COLOR);
      buffer.setTranslation(event.getProjectedPos().scale(-1));

      Vector3d pos = VectorUtils.toFPIVector(current);
      buffer.putOutlinedCuboid(pos, pos.add(1.D, 1.D, 1.D), GeometryMasks.Line.ALL,
          predicted ? Colors.YELLOW : Colors.ORANGE);

      GL11.glEnable(GL11.GL_LINE_SMOOTH);
      buffer.draw();
      GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }
  }
}
