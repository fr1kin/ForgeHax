package dev.fiki.forgehax.main.mods.player;

import com.google.common.collect.Lists;
import dev.fiki.forgehax.api.BlockHelper;
import dev.fiki.forgehax.api.BlockHelper.BlockTraceInfo;
import dev.fiki.forgehax.api.BlockHelper.UniqueBlock;
import dev.fiki.forgehax.api.asm.MapField;
import dev.fiki.forgehax.api.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.api.cmd.settings.DoubleSetting;
import dev.fiki.forgehax.api.cmd.settings.KeyBindingSetting;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.entity.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.api.events.entity.PlayerRotationEvent;
import dev.fiki.forgehax.api.extension.EntityEx;
import dev.fiki.forgehax.api.extension.LocalPlayerEx;
import dev.fiki.forgehax.api.key.KeyConflictContexts;
import dev.fiki.forgehax.api.key.KeyInputs;
import dev.fiki.forgehax.api.math.Angle;
import dev.fiki.forgehax.api.math.VectorUtil;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.api.reflection.types.ReflectionField;
import dev.fiki.forgehax.asm.events.game.BlockControllerProcessEvent;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static dev.fiki.forgehax.main.Common.*;

@RegisterMod(
    name = "Nuker",
    description = "Mine blocks around yourself",
    category = Category.PLAYER
)
@RequiredArgsConstructor
@ExtensionMethod({LocalPlayerEx.class, EntityEx.class})
public class Nuker extends ToggleMod {
  @MapField(parentClass = PlayerController.class, value = "destroyProgress")
  private final ReflectionField<Float> PlayerController_destroyProgress;

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
      .conflictContext(KeyConflictContexts.inGame())
      .build();

  private boolean isTargeting(UniqueBlock block) {
    return targets.stream().anyMatch(b -> b.equals(block.getBlock()));
  }

  private boolean isInBoundary(UniqueBlock ub) {
    if (!bounded.getValue()) {
      return true;
    } else {
      Vector3d pos = ub.getCenteredPos().subtract(getLocalPlayer().position());
      return pos.x < width_upper.getValue()
          && pos.x > -width_lower.getValue()
          && pos.y < height_upper.getValue()
          && pos.y > -height_lower.getValue()
          && pos.z < width_upper.getValue()
          && pos.z > -width_lower.getValue();
    }
  }

  private boolean isNeighborsLiquid(UniqueBlock ub) {
    return filter_liquids.getValue() &&
        Arrays.stream(Direction.values())
            .map(side -> ub.getPos().relative(side))
            .map(getWorld()::getBlockState)
            .map(BlockState::getMaterial)
            .anyMatch(Material::isLiquid);
  }

  private double getHeightBias(UniqueBlock ub) {
    return !y_bias.getValue() ? 0.D : -ub.getCenteredPos().y;
  }

  private float getBlockBreakAmount() {
    return PlayerController_destroyProgress.get(getPlayerController());
  }

  private void updateBlockBreaking(BlockPos target) {
    if (target == null && currentTarget != null) {
      resetBlockBreaking();
    } else if (target != null && currentTarget == null) {
      getPlayerController().stopDestroyBlock();
      currentTarget = target;
    }
  }

  private void resetBlockBreaking() {
    if (currentTarget != null) {
      getPlayerController().stopDestroyBlock();
      currentTarget = null;
    }
  }

  @Override
  protected void onEnabled() {
    printInform("Select blocks by looking at it and pressing %s", selectBind.getKeyName());
  }

  @SubscribeListener
  public void onUpdate(LocalPlayerUpdateEvent event) {
    if (selectBind.isKeyDown() && attackToggle.compareAndSet(false, true)) {
      Block info = Blocks.AIR;
      BlockRayTraceResult tr = getLocalPlayer().getBlockViewTrace();

      if (RayTraceResult.Type.MISS.equals(tr.getType()) && !targets.isEmpty()) {
        Block ub = targets.remove(targets.size() - 1);
        printInform("Removed latest block %s", ub.toString());
        return;
      } else if (RayTraceResult.Type.BLOCK.equals(tr.getType())) {
        info = getWorld().getBlockState(tr.getBlockPos()).getBlock();
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

  @SubscribeListener
  public void onBlockClick(BlockControllerProcessEvent event) {
    if (currentTarget != null) {
      event.setLeftClicked(false); // no block manual breaking while the nuker is running
    }
  }

  @SubscribeListener
  public void onLocalPlayerMovementUpdate(PlayerRotationEvent event) {
    if (targets.isEmpty()) {
      resetBlockBreaking();
      return;
    }

    final ClientPlayerEntity lp = getLocalPlayer();
    final Vector3d eyes = lp.getEyePos();
    final Vector3d dir = client_angles.getValue()
        ? lp.getDirectionVector()
        : lp.getServerDirectionVector();

    BlockTraceInfo trace = null;

    if (currentTarget != null) {
      // verify the current target is still valid
      trace = Optional.of(currentTarget)
          .filter(pos -> !getWorld().isEmptyBlock(pos))
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
      List<UniqueBlock> blocks = BlockHelper.getBlocksInRadius(eyes, getPlayerController().getPickRange())
          .stream()
          .filter(pos -> !getWorld().isEmptyBlock(pos))
          .map(BlockHelper::newUniqueBlock)
          .filter(this::isTargeting)
          .filter(this::isInBoundary)
          .filter(ub -> !isNeighborsLiquid(ub))
          .sorted(Comparator.comparingDouble(this::getHeightBias)
              .thenComparing(ub -> VectorUtil.getCrosshairDistance(eyes, dir, ub.getCenteredPos())))
          .collect(Collectors.toList());

      if (blocks.isEmpty()) {
        resetBlockBreaking();
        return;
      }

      trace = blocks.stream()
          .map(ub -> BlockHelper.getVisibleBlockSideTrace(eyes, dir, ub.getPos()))
          .filter(Objects::nonNull)
          .findFirst()
          .orElse(null);
    }

    if (trace == null) {
      resetBlockBreaking();
      return;
    }

    Angle va = lp.getLookAngles(trace.getHitVec());
    event.setViewAngles(va);

    final BlockTraceInfo tr = trace;
    event.onFocusGained(() -> {
      if (getPlayerController().continueDestroyBlock(tr.getPos(), tr.getOppositeSide())) {
        lp.swingHandSilently();
        updateBlockBreaking(tr.getPos());
      } else {
        resetBlockBreaking();
      }
    });
  }
}
