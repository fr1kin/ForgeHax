package dev.fiki.forgehax.main.mods;

import com.google.common.collect.Lists;
import dev.fiki.forgehax.common.events.BlockControllerProcessEvent;
import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.main.util.cmd.settings.DoubleSetting;
import dev.fiki.forgehax.main.util.cmd.settings.KeyBindingSetting;
import dev.fiki.forgehax.main.util.common.PriorityEnum;
import dev.fiki.forgehax.main.util.entity.EntityUtils;
import dev.fiki.forgehax.main.util.entity.LocalPlayerUtils;
import dev.fiki.forgehax.main.util.key.KeyInputs;
import dev.fiki.forgehax.main.util.math.Angle;
import dev.fiki.forgehax.main.util.math.VectorUtils;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.mods.managers.PositionRotationManager;
import dev.fiki.forgehax.main.mods.managers.PositionRotationManager.RotationState.Local;
import dev.fiki.forgehax.main.util.BlockHelper;
import dev.fiki.forgehax.main.util.BlockHelper.BlockTraceInfo;
import dev.fiki.forgehax.main.util.BlockHelper.UniqueBlock;
import dev.fiki.forgehax.main.util.Utils;
import dev.fiki.forgehax.main.util.key.BindingHelper;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import dev.fiki.forgehax.main.util.reflection.FastReflection;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import static dev.fiki.forgehax.main.Common.*;

@RegisterMod
public class Nuker extends ToggleMod implements PositionRotationManager.MovementUpdateListener {

  private final List<Block> targets = Lists.newArrayList();
  private final AtomicBoolean attackToggle = new AtomicBoolean(false);

  private BlockPos currentTarget = null;

  private final BooleanSetting client_angles = newBooleanSetting()
      .name("client-angles")
      .description("Sort the blocks to break by the clients angle instead of the servers")
      .defaultTo(false)
      .build();

  private final BooleanSetting bounded = newBooleanSetting()
      .name("bounded")
      .description("Bound the nuker to a limited radius from the player")
      .defaultTo(false)
      .build();

  private final DoubleSetting height_upper = newDoubleSetting()
      .name("height-upper")
      .description("Upper height (Y axis) limit")
      .defaultTo(10.D)
      .min(0.D)
      .max(10.D)
      .build();

  private final DoubleSetting height_lower = newDoubleSetting()
      .name("height-lower")
      .description("Lower height (Y axis) limit")
      .defaultTo(10.D)
      .min(0.D)
      .max(10.D)
      .build();

  private final DoubleSetting width_upper = newDoubleSetting()
      .name("width-upper")
      .description("Upper width (X and Z axis) limit")
      .defaultTo(10.D)
      .min(0.D)
      .max(10.D)
      .build();

  private final DoubleSetting width_lower = newDoubleSetting()
      .name("width-lower")
      .description("Lower width (X and Z axis) limit")
      .defaultTo(10.D)
      .min(0.D)
      .max(10.D)
      .build();

  private final BooleanSetting filter_liquids = newBooleanSetting()
      .name("filter-liquids")
      .description("Will not mine blocks that is a neighbors to a liquid block.")
      .defaultTo(false)
      .build();

  private final BooleanSetting y_bias = newBooleanSetting()
      .name("y-bias")
      .description("Will prefer higher blocks (good for mining sand).")
      .defaultTo(false)
      .build();

  private final KeyBindingSetting selectBind = newKeyBindingSetting()
      .name("select-bind")
      .description("Bind for the selection action")
      .keyName("Selection")
      .defaultKeyCategory()
      .key(KeyInputs.MOUSE_LEFT)
      .build();

  public Nuker() {
    super(Category.PLAYER, "Nuker", false, "Mine blocks around yourself");
  }

  private boolean isTargeting(UniqueBlock block) {
    return targets.stream().anyMatch(b -> b.equals(block.getBlock()));
  }

  private boolean isInBoundary(UniqueBlock ub) {
    if (!bounded.getValue()) {
      return true;
    } else {
      Vec3d pos = ub.getCenteredPos().subtract(getLocalPlayer().getPositionVector());
      return pos.x < width_upper.getValue()
          && pos.x > -width_lower.getValue()
          && pos.y < height_upper.getValue()
          && pos.y > -height_lower.getValue()
          && pos.z < width_upper.getValue()
          && pos.z > -width_lower.getValue();
    }
  }

  private boolean isNeighborsLiquid(UniqueBlock ub) {
    return filter_liquids.getValue()
        && Arrays.stream(Direction.values())
        .map(side -> ub.getPos().offset(side))
        .map(getWorld()::getBlockState)
        .map(BlockState::getMaterial)
        .anyMatch(Material::isLiquid);
  }

  private double getHeightBias(UniqueBlock ub) {
    return !y_bias.getValue() ? 0.D : -ub.getCenteredPos().y;
  }

  private float getBlockBreakAmount() {
    return FastReflection.Fields.PlayerController_curBlockDamageMP.get(getPlayerController());
  }

  private void updateBlockBreaking(BlockPos target) {
    if (target == null && currentTarget != null) {
      resetBlockBreaking();
    } else if (target != null && currentTarget == null) {
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
    printInform("Select blocks by looking at it and pressing %s", selectBind.getKeyName());
  }

  @Override
  protected void onDisabled() {
    PositionRotationManager.getManager().unregister(this);
  }

  @SubscribeEvent
  public void onUpdate(LocalPlayerUpdateEvent event) {
    if (selectBind.isKeyDown() && attackToggle.compareAndSet(false, true)) {
      Block info = Blocks.AIR;
      BlockRayTraceResult tr = LocalPlayerUtils.getBlockViewTrace();

      getLogger().info(tr);

      if (RayTraceResult.Type.MISS.equals(tr.getType()) && !targets.isEmpty()) {
        Block ub = targets.remove(targets.size() - 1);
        printInform("Removed latest block %s", ub.toString());
        return;
      } else if (RayTraceResult.Type.BLOCK.equals(tr.getType())) {
        info = getWorld().getBlockState(tr.getPos()).getBlock();
      }

      if (Blocks.AIR.equals(info)) {
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
    } else if (!selectBind.isKeyDown()) {
      attackToggle.set(false);
    }
  }

  @SubscribeEvent
  public void onBlockClick(BlockControllerProcessEvent event) {
    if (currentTarget != null) {
      event.setLeftClicked(false); // no block manual breaking while the nuker is running
    }
  }

  @Override
  public void onLocalPlayerMovementUpdate(Local state) {
    if (targets.isEmpty()) {
      resetBlockBreaking();
      return;
    }

    final Vec3d eyes = EntityUtils.getEyePos(getLocalPlayer());
    final Vec3d dir =
        client_angles.getValue()
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
      if (trace == null) {
        resetBlockBreaking();
      }
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
            getNetworkManager().sendPacket(new CAnimateHandPacket(Hand.MAIN_HAND));
            updateBlockBreaking(tr.getPos());
          } else {
            resetBlockBreaking();
          }
        });
  }
}
