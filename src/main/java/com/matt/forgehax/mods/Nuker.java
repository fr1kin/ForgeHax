package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getNetworkManager;
import static com.matt.forgehax.Helper.getPlayerController;
import static com.matt.forgehax.Helper.getWorld;
import static com.matt.forgehax.Helper.printError;
import static com.matt.forgehax.Helper.printInform;
import static com.matt.forgehax.Helper.printWarning;

import com.google.common.collect.Lists;
import com.matt.forgehax.asm.events.BlockControllerProcessEvent;
import com.matt.forgehax.asm.reflection.FastReflection.Fields;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.mods.managers.PositionRotationManager;
import com.matt.forgehax.mods.managers.PositionRotationManager.RotationState.Local;
import com.matt.forgehax.util.BlockHelper;
import com.matt.forgehax.util.BlockHelper.BlockTraceInfo;
import com.matt.forgehax.util.BlockHelper.UniqueBlock;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.common.PriorityEnum;
import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.entity.LocalPlayerUtils;
import com.matt.forgehax.util.key.BindingHelper;
import com.matt.forgehax.util.math.Angle;
import com.matt.forgehax.util.math.VectorUtils;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class Nuker extends ToggleMod implements PositionRotationManager.MovementUpdateListener {
  private final KeyBinding bindSelect = new KeyBinding("Nuker Selection", -98, "ForgeHax");

  private final List<UniqueBlock> targets = Lists.newArrayList();
  private final AtomicBoolean attackToggle = new AtomicBoolean(false);

  private BlockPos currentTarget = null;

  private final Setting<Boolean> client_angles =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("client-angles")
          .description("Sort the blocks to break by the clients angle instead of the servers")
          .defaultTo(false)
          .build();

  private final Setting<Boolean> bounded =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("bounded")
          .description("Bound the nuker to a limited radius from the player")
          .defaultTo(false)
          .build();

  private final Setting<Double> height_upper =
      getCommandStub()
          .builders()
          .<Double>newSettingBuilder()
          .name("height-upper")
          .description("Upper height (Y axis) limit")
          .defaultTo(10.D)
          .min(0.D)
          .max(10.D)
          .build();
  private final Setting<Double> height_lower =
      getCommandStub()
          .builders()
          .<Double>newSettingBuilder()
          .name("height-lower")
          .description("Lower height (Y axis) limit")
          .defaultTo(10.D)
          .min(0.D)
          .max(10.D)
          .build();

  private final Setting<Double> width_upper =
      getCommandStub()
          .builders()
          .<Double>newSettingBuilder()
          .name("width-upper")
          .description("Upper width (X and Z axis) limit")
          .defaultTo(10.D)
          .min(0.D)
          .max(10.D)
          .build();
  private final Setting<Double> width_lower =
      getCommandStub()
          .builders()
          .<Double>newSettingBuilder()
          .name("width-lower")
          .description("Lower width (X and Z axis) limit")
          .defaultTo(10.D)
          .min(0.D)
          .max(10.D)
          .build();

  private final Setting<Boolean> filter_liquids =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("filter-liquids")
          .description("Will not mine blocks that is a neighbors to a liquid block.")
          .defaultTo(false)
          .build();

  private final Setting<Boolean> y_bias =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("y-bias")
          .description("Will prefer higher blocks (good for mining sand).")
          .defaultTo(false)
          .build();

  public Nuker() {
    super(Category.PLAYER, "Nuker", false, "Mine blocks around yourself");
    this.bindSelect.setKeyConflictContext(BindingHelper.getEmptyKeyConflictContext());
    ClientRegistry.registerKeyBinding(this.bindSelect);
  }

  private boolean isTargeting(UniqueBlock ub) {
    return targets.stream().anyMatch(ub::equals);
  }

  private boolean isInBoundary(UniqueBlock ub) {
    if (!bounded.get()) return true;
    else {
      Vec3d pos = ub.getCenteredPos().subtract(getLocalPlayer().getPositionVector());
      return pos.x < width_upper.get()
          && pos.x > -width_lower.get()
          && pos.y < height_upper.get()
          && pos.y > -height_lower.get()
          && pos.z < width_upper.get()
          && pos.z > -width_lower.get();
    }
  }

  private boolean isNeighborsLiquid(UniqueBlock ub) {
    return filter_liquids.get()
        && Arrays.stream(EnumFacing.values())
            .map(side -> ub.getPos().offset(side))
            .map(pos -> getWorld().getBlockState(pos).getBlock())
            .anyMatch(BlockLiquid.class::isInstance);
  }

  private double getHeightBias(UniqueBlock ub) {
    return !y_bias.get() ? 0.D : -ub.getCenteredPos().y;
  }

  private float getBlockBreakAmount() {
    return Fields.PlayerControllerMP_curBlockDamageMP.get(getPlayerController());
  }

  private void updateBlockBreaking(BlockPos target) {
    if (target == null && currentTarget != null) resetBlockBreaking();
    else if (target != null && currentTarget == null) {
      getPlayerController().resetBlockRemoving();
      currentTarget = target;
    }
  }

  private void resetBlockBreaking() {
    if (currentTarget != null) {
      getPlayerController().resetBlockRemoving();
      currentTarget = null;
    }
  }

  @Override
  protected void onEnabled() {
    PositionRotationManager.getManager().register(this, PriorityEnum.HIGH);
    printInform(
        "Select blocks by looking at it and pressing %s", BindingHelper.getIndexName(bindSelect));
  }

  @Override
  protected void onDisabled() {
    PositionRotationManager.getManager().unregister(this);
  }

  @SubscribeEvent
  public void onUpdate(LocalPlayerUpdateEvent event) {
    if (bindSelect.isKeyDown() && attackToggle.compareAndSet(false, true)) {
      UniqueBlock info = null;
      RayTraceResult tr = LocalPlayerUtils.getMouseOverBlockTrace();

      if (tr == null && !targets.isEmpty()) {
        UniqueBlock ub = targets.remove(targets.size() - 1);
        printInform("Removed latest block %s", ub.toString());
        return;
      } else if (tr != null) info = BlockHelper.newUniqueBlock(tr.getBlockPos());

      if (info == null) return;

      if (info.isInvalid()) {
        printWarning("Invalid block selected!");
        return;
      }

      if (!targets.contains(info) && targets.add(info)) {
        printInform("Added block %s", info.toString());
      } else if (targets.remove(info)) {
        printInform("Removed block %s", info.toString());
      } else {
        printError("Unknown error adding or removing block %s", info.toString());
      }
    } else if (!bindSelect.isKeyDown()) {
      attackToggle.set(false);
    }
  }

  @SubscribeEvent
  public void onBlockClick(BlockControllerProcessEvent event) {
    if (currentTarget != null)
      event.setLeftClicked(false); // no block manual breaking while the nuker is running
  }

  @Override
  public void onLocalPlayerMovementUpdate(Local state) {
    if (targets.isEmpty()) {
      resetBlockBreaking();
      return;
    }

    final Vec3d eyes = EntityUtils.getEyePos(getLocalPlayer());
    final Vec3d dir =
        client_angles.get()
            ? LocalPlayerUtils.getDirectionVector()
            : LocalPlayerUtils.getServerDirectionVector();

    BlockTraceInfo trace = null;

    if (currentTarget != null) {
      // verify the current target is still valid
      trace =
          Optional.of(currentTarget)
              .filter(pos -> !getWorld().isAirBlock(pos))
              .map(BlockHelper::newUniqueBlock)
              .filter(this::isTargeting)
              .filter(this::isInBoundary)
              .filter(ub -> !isNeighborsLiquid(ub))
              .map(ub -> BlockHelper.getVisibleBlockSideTrace(eyes, dir, ub.getPos()))
              .orElse(null);
      if (trace == null) resetBlockBreaking();
    }

    if (currentTarget == null) {
      List<UniqueBlock> blocks =
          BlockHelper.getBlocksInRadius(eyes, getPlayerController().getBlockReachDistance())
              .stream()
              .filter(pos -> !getWorld().isAirBlock(pos))
              .map(BlockHelper::newUniqueBlock)
              .filter(this::isTargeting)
              .filter(this::isInBoundary)
              .filter(ub -> !isNeighborsLiquid(ub))
              .sorted(
                  Comparator.comparingDouble(this::getHeightBias)
                      .thenComparing(
                          ub -> VectorUtils.getCrosshairDistance(eyes, dir, ub.getCenteredPos())))
              .collect(Collectors.toList());

      if (blocks.isEmpty()) {
        resetBlockBreaking();
        return;
      }

      trace =
          blocks
              .stream()
              .map(ub -> BlockHelper.getVisibleBlockSideTrace(eyes, dir, ub.getPos()))
              .filter(Objects::nonNull)
              .findFirst()
              .orElse(null);
    }

    if (trace == null) {
      resetBlockBreaking();
      return;
    }

    Angle va = Utils.getLookAtAngles(trace.getHitVec());
    state.setServerAngles(va);

    final BlockTraceInfo tr = trace;
    state.invokeLater(
        rs -> {
          if (getPlayerController().onPlayerDamageBlock(tr.getPos(), tr.getOppositeSide())) {
            getNetworkManager().sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
            updateBlockBreaking(tr.getPos());
          } else resetBlockBreaking();
        });
  }
}
