package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getNetworkManager;
import static com.matt.forgehax.Helper.getPlayerController;
import static com.matt.forgehax.Helper.getWorld;

import com.matt.forgehax.asm.reflection.FastReflection.Fields;
import com.matt.forgehax.mods.managers.PositionRotationManager;
import com.matt.forgehax.mods.managers.PositionRotationManager.RotationState.Local;
import com.matt.forgehax.mods.services.HotbarSelectionService.ResetFunction;
import com.matt.forgehax.util.BlockHelper;
import com.matt.forgehax.util.BlockHelper.BlockTraceInfo;
import com.matt.forgehax.util.PacketHelper;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.common.PriorityEnum;
import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.entity.LocalPlayerInventory;
import com.matt.forgehax.util.entity.LocalPlayerInventory.InvItem;
import com.matt.forgehax.util.entity.LocalPlayerUtils;
import com.matt.forgehax.util.math.Angle;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@RegisterMod
public class Scaffold extends ToggleMod implements PositionRotationManager.MovementUpdateListener {
  
  private static final EnumSet<EnumFacing> NEIGHBORS =
      EnumSet.of(EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST);
  
  private int tickCount = 0;
  private boolean placing = false;
  private Angle previousAngles = Angle.ZERO;
  
  public Scaffold() {
    super(Category.MOVEMENT, "Scaffold", false, "Place blocks under yourself");
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
    
    if (LocalPlayerUtils.getVelocity().normalize().lengthVector() > 1.D && placing) {
      state.setServerAngles(previousAngles);
    } else {
      placing = false;
      tickCount = 0;
    }
    
    BlockPos below = new BlockPos(getLocalPlayer()).down();
    
    if (!getWorld().getBlockState(below).getMaterial().isReplaceable()) {
      return;
    }
    
    InvItem items =
        LocalPlayerInventory.getHotbarInventory()
            .stream()
            .filter(InvItem::nonNull)
            .filter(item -> item.getItem() instanceof ItemBlock)
            .filter(item -> Block.getBlockFromItem(item.getItem()).getDefaultState().isFullBlock())
            .max(Comparator.comparingInt(LocalPlayerInventory::getHotbarDistance))
            .orElse(InvItem.EMPTY);
    
    if (items.isNull()) {
      return;
    }
    
    final Vec3d eyes = EntityUtils.getEyePos(getLocalPlayer());
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
                new CPacketEntityAction(getLocalPlayer(), Action.START_SNEAKING));
            
            LocalPlayerUtils.setSneaking(true);
            LocalPlayerUtils.setSneakingSuppression(true);
          }
          
          getPlayerController()
              .processRightClickBlock(
                  getLocalPlayer(),
                  getWorld(),
                  tr.getPos(),
                  tr.getOppositeSide(),
                  hit,
                  EnumHand.MAIN_HAND);
          
          getNetworkManager().sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
          
          if (sneak) {
            LocalPlayerUtils.setSneaking(false);
            LocalPlayerUtils.setSneakingSuppression(false);
            
            getNetworkManager()
                .sendPacket(new CPacketEntityAction(getLocalPlayer(), Action.STOP_SNEAKING));
          }
          
          func.revert();
          
          Fields.Minecraft_rightClickDelayTimer.set(MC, 4);
          placing = true;
          tickCount = 0;
        });
  }
}
