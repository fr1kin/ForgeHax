package dev.fiki.forgehax.main.mods;

import static net.minecraft.network.play.client.CEntityActionPacket.*;

import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.common.PriorityEnum;
import dev.fiki.forgehax.main.util.entity.EntityUtils;
import dev.fiki.forgehax.main.util.entity.LocalPlayerInventory;
import dev.fiki.forgehax.main.util.entity.LocalPlayerUtils;
import dev.fiki.forgehax.main.util.math.Angle;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.mods.managers.PositionRotationManager;
import dev.fiki.forgehax.main.mods.managers.PositionRotationManager.RotationState.Local;
import dev.fiki.forgehax.main.mods.services.HotbarSelectionService.ResetFunction;
import dev.fiki.forgehax.main.util.BlockHelper;
import dev.fiki.forgehax.main.util.BlockHelper.BlockTraceInfo;
import dev.fiki.forgehax.main.util.PacketHelper;
import dev.fiki.forgehax.main.util.Utils;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;

import dev.fiki.forgehax.main.util.reflection.FastReflection;
import net.minecraft.item.BlockItem;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@RegisterMod
public class Scaffold extends ToggleMod implements PositionRotationManager.MovementUpdateListener {

  private static final EnumSet<Direction> NEIGHBORS =
      EnumSet.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);

  private int tickCount = 0;
  private boolean placing = false;
  private Angle previousAngles = Angle.ZERO;

  public Scaffold() {
    super(Category.PLAYER, "Scaffold", false, "Place blocks under yourself");
  }

  @Override
  protected void onEnabled() {
    PositionRotationManager.getManager().register(this, PriorityEnum.HIGHEST);
  }

  @Override
  protected void onDisabled() {
    PositionRotationManager.getManager().unregister(this);
  }

  @Override
  public void onLocalPlayerMovementUpdate(Local state) {
    if (placing) {
      ++tickCount;
    }

    if (LocalPlayerUtils.getVelocity().normalize().length() > 1.D && placing) {
      state.setServerAngles(previousAngles);
    } else {
      placing = false;
      tickCount = 0;
    }

    BlockPos below = new BlockPos(Common.getLocalPlayer()).down();

    if (!Common.getWorld().getBlockState(below).getMaterial().isReplaceable()) {
      return;
    }

    LocalPlayerInventory.InvItem items =
        LocalPlayerInventory.getHotbarInventory()
            .stream()
            .filter(LocalPlayerInventory.InvItem::nonNull)
            .filter(item -> item.getItem() instanceof BlockItem)
            //.filter(item -> Block.getBlockFromItem(item.getItem()).getDefaultState().getCollisionShape())
            // TODO: 1.15 find way to detect if its a full block
            .max(Comparator.comparingInt(LocalPlayerInventory::getHotbarDistance))
            .orElse(LocalPlayerInventory.InvItem.EMPTY);

    if (items.isNull()) {
      return;
    }

    final Vec3d eyes = EntityUtils.getEyePos(Common.getLocalPlayer());
    final Vec3d dir = LocalPlayerUtils.getViewAngles().getDirectionVector();

    BlockTraceInfo trace =
        Optional.ofNullable(BlockHelper.getPlaceableBlockSideTrace(eyes, dir, below))
            .filter(tr -> tr.isPlaceable(items))
            .orElseGet(
                () ->
                    NEIGHBORS
                        .stream()
                        .map(below::offset)
                        .filter(BlockHelper::isBlockReplaceable)
                        .map(bp -> BlockHelper.getPlaceableBlockSideTrace(eyes, dir, bp))
                        .filter(Objects::nonNull)
                        .filter(tr -> tr.isPlaceable(items))
                        .max(Comparator.comparing(BlockTraceInfo::isSneakRequired))
                        .orElse(null));

    if (trace == null) {
      return;
    }

    Vec3d hit = trace.getHitVec();
    state.setServerAngles(previousAngles = Utils.getLookAtAngles(hit));

    final BlockTraceInfo tr = trace;
    state.invokeLater(
        rs -> {
          ResetFunction func = LocalPlayerInventory.setSelected(items);

          boolean sneak = tr.isSneakRequired() && !LocalPlayerUtils.isSneaking();
          if (sneak) {
            // send start sneaking packet
            PacketHelper.ignoreAndSend(
                new CEntityActionPacket(Common.getLocalPlayer(), Action.PRESS_SHIFT_KEY));

            LocalPlayerUtils.setSneaking(true);
            LocalPlayerUtils.setSneakingSuppression(true);
          }

          Common.getPlayerController()
              .processRightClick(
                  Common.getLocalPlayer(),
                  Common.getWorld(),
                  Hand.MAIN_HAND);

          Common.sendNetworkPacket(new CAnimateHandPacket(Hand.MAIN_HAND));

          if (sneak) {
            LocalPlayerUtils.setSneaking(false);
            LocalPlayerUtils.setSneakingSuppression(false);

            Common.sendNetworkPacket(new CEntityActionPacket(Common.getLocalPlayer(), Action.RELEASE_SHIFT_KEY));
          }

          func.revert();

          FastReflection.Fields.Minecraft_rightClickDelayTimer.set(Common.MC, 4);
          placing = true;
          tickCount = 0;
        });
  }
}
