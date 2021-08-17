package dev.fiki.forgehax.main.mods.player;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.matrix.MatrixStack;
import dev.fiki.forgehax.api.BlockHelper;
import dev.fiki.forgehax.api.BlockHelper.BlockTraceInfo;
import dev.fiki.forgehax.api.BlockHelper.UniqueBlock;
import dev.fiki.forgehax.api.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.api.cmd.settings.IntegerSetting;
import dev.fiki.forgehax.api.cmd.settings.KeyBindingSetting;
import dev.fiki.forgehax.api.cmd.settings.collections.SimpleSettingSet;
import dev.fiki.forgehax.api.color.Colors;
import dev.fiki.forgehax.api.draw.GeometryMasks;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.entity.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.api.events.entity.PlayerRotationEvent;
import dev.fiki.forgehax.api.events.render.RenderSpaceEvent;
import dev.fiki.forgehax.api.extension.*;
import dev.fiki.forgehax.api.key.KeyConflictContexts;
import dev.fiki.forgehax.api.key.KeyInputs;
import dev.fiki.forgehax.api.math.Angle;
import dev.fiki.forgehax.api.math.VectorUtil;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.api.reflection.ReflectionTools;
import dev.fiki.forgehax.main.services.SneakService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import lombok.val;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static dev.fiki.forgehax.main.Common.*;

@RegisterMod(
    name = "AutoPlace",
    description = "Automatically place blocks on top of other blocks",
    category = Category.PLAYER
)
@RequiredArgsConstructor
@ExtensionMethod({GeneralEx.class, ItemEx.class, LocalPlayerEx.class, EntityEx.class, VectorEx.class, VertexBuilderEx.class})
public class AutoPlace extends ToggleMod {
  enum Stage {
    SELECT_BLOCKS,
    SELECT_REPLACEMENT,
    CONFIRM,
    READY,
    ;
  }

  private final SneakService sneaks;
  private final ReflectionTools reflection;

  private final SimpleSettingSet<Direction> sides = newSimpleSettingEnumSet(Direction.class)
      .name("sides")
      .description("Sides to place the blocks on")
      .build();

  private final BooleanSetting check_neighbors = newBooleanSetting()
      .name("check-neighbors")
      .description("Will check the neighboring blocks to see if a block can be placed")
      .defaultTo(false)
      .build();

  private final BooleanSetting whitelist = newBooleanSetting()
      .name("whitelist")
      .description("Makes the target list function as an inclusive list")
      .defaultTo(true)
      .build();

  private final BooleanSetting silent = newBooleanSetting()
      .name("silent")
      .description("Client angles don't change")
      .defaultTo(true)
      .build();

  private final IntegerSetting cooldown = newIntegerSetting()
      .name("cooldown")
      .description(
          "Block place delay to check_neighbors after placing a block. Set to 0 to disable")
      .defaultTo(4)
      .min(0)
      .build();

  private final BooleanSetting render = newBooleanSetting()
      .name("render")
      .description("Show which blocks are currently visible and being targeted")
      .defaultTo(false)
      .changedListener((from, to) -> {
        if (to) {
          this.renderingBlocks.clear();
          this.currentRenderingTarget = null;
        }
      })
      .build();

  private final BooleanSetting client_angles = newBooleanSetting()
      .name("client-angles")
      .description("Sort the blocks to break by the clients angle instead of the servers")
      .defaultTo(false)
      .build();

  private final KeyBindingSetting bindSelect = newKeyBindingSetting()
      .name("select-bind")
      .description("Bind for selection")
      .keyName("Select")
      .defaultKeyCategory()
      .key(KeyInputs.MOUSE_LEFT)
      .conflictContext(KeyConflictContexts.inGame())
      .build();

  private final KeyBindingSetting bindFinish = newKeyBindingSetting()
      .name("finish-bind")
      .description("Bind for finishing")
      .keyName("Finish")
      .defaultKeyCategory()
      .key(KeyInputs.MOUSE_RIGHT)
      .conflictContext(KeyConflictContexts.inGame())
      .build();

  private final Set<BlockPos> renderingBlocks = Sets.newConcurrentHashSet();
  private BlockPos currentRenderingTarget = null;

  private final AtomicBoolean bindSelectToggle = new AtomicBoolean(false);
  private final AtomicBoolean bindFinishToggle = new AtomicBoolean(false);
  private final AtomicBoolean printToggle = new AtomicBoolean(false);
  private final AtomicBoolean resetToggle = new AtomicBoolean(false);

  private final List<UniqueBlock> targets = Lists.newArrayList();

  private ItemStack selectedItem = null;

  private Runnable resetTask = null;

  private Stage stage = Stage.SELECT_BLOCKS;

  private void reset() {
    if (resetToggle.compareAndSet(true, false)) {
      targets.clear();
      selectedItem = null;
      stage = Stage.SELECT_BLOCKS;
      printToggle.set(false);
      if (resetTask != null) {
        resetTask.run();
        resetTask = null;
      }
      printInform("AutoPlace data has been reset.");
    }
  }

  private boolean isValidBlock(UniqueBlock info) {
    return whitelist.getValue()
        ? targets.stream().anyMatch(info::equals)
        : targets.stream().noneMatch(info::equals);
  }

  private boolean isClickable(UniqueBlock info) {
    return sides.stream()
        .anyMatch(side -> BlockHelper.isBlockReplaceable(info.getPos().relative(side)));
  }

  private void showInfo(String filter) {
    addScheduledTask(() -> {
      if ("selected".startsWith(filter)) {
        printInform("Selected item %s", this.selectedItem.getDisplayName().getString());
      }

      if ("targets".startsWith(filter)) {
        printInform(
            "Targets: %s",
            this.targets.stream().map(UniqueBlock::toString).collect(Collectors.joining(", ")));
      }

      if ("sides".startsWith(filter)) {
        printInform(
            "Sides: %s",
            this.sides.stream()
                .map(Direction::getName)
                .collect(Collectors.joining(", ")));
      }

      if ("whitelist".startsWith(filter)) {
        printInform("Whitelist: %s", Boolean.toString(whitelist.getValue()));
      }

      if ("check_neighbors".startsWith(filter)) {
        printInform("Check Neighbors: %s", Boolean.toString(check_neighbors.getValue()));
      }
    });
  }

  @Override
  protected void onDisabled() {
    printToggle.set(false);
  }

  @SubscribeListener
  public void onRender(RenderSpaceEvent event) {
    if (!render.getValue() || MC.getCameraEntity() == null) {
      return;
    }

    final MatrixStack stack = event.getStack();
    final BufferBuilder builder = event.getBuffer();
    final Vector3d renderPos = getLocalPlayer().getInterpolatedPos(MC.getDeltaFrameTime());

    builder.beginLines(DefaultVertexFormats.POSITION_COLOR);

    for (BlockPos pos : renderingBlocks) {
      stack.pushPose();

      final BlockState state = getWorld().getBlockState(pos);
      final AxisAlignedBB bb = state.getCollisionShape(getWorld(), pos).bounds();

      stack.translateVec(pos.subtract(renderPos));
      builder.outlinedCube(bb, GeometryMasks.Line.ALL, Colors.GREEN.setAlpha(150), stack.getLastMatrix());

      stack.popPose();
    }

    // poz
    final BlockPos current = this.currentRenderingTarget;

    if (current != null) {
      stack.pushPose();

      final BlockState state = getWorld().getBlockState(current);
      final AxisAlignedBB bb = state.getCollisionShape(getWorld(), current).bounds();

      stack.translateVec(current.subtract(renderPos));
      builder.outlinedCube(bb, GeometryMasks.Line.ALL, Colors.RED.setAlpha(150), stack.getLastMatrix());

      stack.popPose();
    }

    builder.draw();
  }

  @SubscribeListener
  public void onUpdate(LocalPlayerUpdateEvent event) {
    reset();

    switch (stage) {
      case SELECT_BLOCKS: {
        if (printToggle.compareAndSet(false, true)) {
          targets.clear();
          printInform("Select blocks by pressing %s", bindSelect.getKeyName());
          printInform("Finish this stage by pressing %s", bindFinish.getKeyName());
        }

        if (bindSelect.isKeyDown() && bindSelectToggle.compareAndSet(false, true)) {
          final BlockRayTraceResult tr = getLocalPlayer().getBlockViewTrace();
          if (RayTraceResult.Type.MISS.equals(tr.getType())) {
            return;
          }

          UniqueBlock info = BlockHelper.newUniqueBlock(tr.getBlockPos());

          if (info.isInvalid()) {
            printWarning("Invalid block %s", info.toString());
            return;
          }

          if (!targets.contains(info)) {
            printInform("Added block %s", info.toString());
            targets.add(info);
          } else {
            printInform("Removed block %s", info.toString());
            targets.remove(info);
          }
        } else if (!bindSelect.isKeyDown()) {
          bindSelectToggle.set(false);
        }

        if (bindFinish.isKeyDown() && bindFinishToggle.compareAndSet(false, true)) {
          if (targets.isEmpty()) {
            printWarning("No items have been selected yet!");
          } else {
            stage = Stage.SELECT_REPLACEMENT;
            printToggle.set(false);
          }
        } else if (!bindFinish.isKeyDown()) {
          bindFinishToggle.set(false);
        }
        break;
      }
      case SELECT_REPLACEMENT: {
        if (printToggle.compareAndSet(false, true)) {
          printInform(
              "Hover over the block in your hot bar you want to place and press %s to select",
              bindSelect.getKeyName());
        }

        if (bindSelect.isKeyDown() && bindSelectToggle.compareAndSet(false, true)) {
          final ItemStack selected = getLocalPlayer().getOffhandItem();

          if (selected.isEmpty()) {
            printWarning("No item selected!");
            return;
          }

          this.selectedItem = new ItemStack(selected.getItem(), 1);

          printInform("Selected item %s", this.selectedItem.getItem().getRegistryName());

          stage = Stage.CONFIRM;
          printToggle.set(false);
        } else if (!bindSelect.isKeyDown()) {
          bindSelectToggle.set(false);
        }
        break;
      }
      case CONFIRM: {
        if (printToggle.compareAndSet(false, true)) {
          printInform(
              "Press %s to begin, or '.%s info' to set the current settings",
              bindFinish.getKeyName(), getName());
        }

        if (bindFinish.isKeyDown()
            && selectedItem != null
            && bindFinishToggle.compareAndSet(false, true)) {
          printInform("Block place process started");
          printInform("Type '.%s reset' to restart the process", getName());
          stage = Stage.READY;
        } else if (!bindFinish.isKeyDown()) {
          bindFinishToggle.set(false);
        }
        break;
      }
      case READY: {
        if (bindFinish.isKeyDown() && bindFinishToggle.compareAndSet(false, true)) {
          printInform("Block place process paused");
          stage = Stage.CONFIRM;
        } else if (!bindFinish.isKeyDown()) {
          bindFinishToggle.set(false);
        }
        break;
      }
    }
  }

  @SubscribeListener
  public void onLocalPlayerMovementUpdate(PlayerRotationEvent event) {
    if (!Stage.READY.equals(stage)) {
      renderingBlocks.clear();
      currentRenderingTarget = null;
      return;
    }
    if (cooldown.getValue() > 0 && reflection.Minecraft_rightClickDelay.get(MC) > 0) {
      return;
    }

    if (render.getValue()) {
      renderingBlocks.clear();
      currentRenderingTarget = null;
    }

    val lp = getLocalPlayer();
    final Slot placingBlocks = lp.getHotbarSlots().stream()
        .filter(Slot::hasItem)
        .filter(slot -> slot.getItem().sameItem(selectedItem))
        .filter(slot -> lp.canPlaceBlock(slot.getItem().getBlockForItem()))
        .findAny()
        .orElse(null);

    if (placingBlocks == null) {
      return;
    }

    final Vector3d eyes = lp.getEyePos();
    final Vector3d dir = client_angles.getValue()
        ? lp.getDirectionVector()
        : lp.getServerDirectionVector();

    List<UniqueBlock> blocks =
        BlockHelper.getBlocksInRadius(eyes, getPlayerController().getPickRange()).stream()
            .filter(pos -> !getWorld().isEmptyBlock(pos))
            .map(BlockHelper::newUniqueBlock)
            .filter(this::isValidBlock)
            .filter(this::isClickable)
            .sorted(Comparator.comparingDouble(info ->
                VectorUtil.getCrosshairDistance(eyes, dir, BlockHelper.getOBBCenter(info.getPos()))))
            .collect(Collectors.toList());

    if (blocks.isEmpty()) {
      return;
    }

    if (render.getValue()) {
      currentRenderingTarget = null;
      renderingBlocks.clear();
      renderingBlocks.addAll(blocks.stream().map(UniqueBlock::getPos).collect(Collectors.toSet()));
    }

    // find a block that can be placed
    int index = 0;
    BlockTraceInfo trace = null;
    do {
      if (index >= blocks.size()) {
        break;
      }

      final UniqueBlock at = blocks.get(index++);
      if (!check_neighbors.getValue()) {
        trace = sides.stream()
            .map(side -> BlockHelper.getBlockSideTrace(eyes, at.getPos(), side))
            .filter(Objects::nonNull)
            .filter(tr -> lp.canPlaceBlock(placingBlocks.getItem().getBlockForItem(), tr.getPos()))
            .max(Comparator.comparing(BlockTraceInfo::isSneakRequired)
                .thenComparing(i -> -VectorUtil.getCrosshairDistance(eyes, dir, i.getCenterPos())))
            .orElse(null);
      } else {
        trace = sides.stream()
            .map(side -> BlockHelper.getPlaceableBlockSideTrace(eyes, dir, at.getPos().relative(side)))
            .filter(Objects::nonNull)
            .filter(tr -> lp.canPlaceBlock(placingBlocks.getItem().getBlockForItem(), tr.getPos()))
            .max(Comparator.comparing(BlockTraceInfo::isSneakRequired)
                .thenComparing(i -> -VectorUtil.getCrosshairDistance(eyes, dir, i.getCenterPos())))
            .orElse(null);
      }
    } while (trace == null);

    // if the block list is exhausted
    if (trace == null) {
      return;
    }

    if (render.getValue()) {
      currentRenderingTarget = trace.getPos();
    }

    Angle va = lp.getLookAngles(trace.getHitVec());
    event.setViewAngles(va);
    event.setSilent(silent.isEnabled());

    final BlockTraceInfo tr = trace;
    event.onFocusGained(() -> {
      final Runnable resetSelected = lp.setSelectedSlot(placingBlocks, ticks -> true);

      boolean sneak = tr.isSneakRequired() && !lp.isCrouchSneaking();
      if (sneak) {
        // send start sneaking packet
        getNetworkManager().dispatchSilentNetworkPacket(new CEntityActionPacket(lp,
            CEntityActionPacket.Action.PRESS_SHIFT_KEY));

        sneaks.setSuppressing(true);
        sneaks.setSneaking(true);
      }

      val blockTr = new BlockRayTraceResult(tr.getHitVec(), tr.getOppositeSide(), tr.getPos(), false);
      if (lp.placeBlock(Hand.MAIN_HAND, blockTr).consumesAction()) {
        // stealth send swing packet
        lp.swingHandSilently();
      }

      if (sneak) {
        sneaks.setSneaking(false);
        sneaks.setSuppressing(false);

        getNetworkManager().dispatchNetworkPacket(new CEntityActionPacket(lp,
            CEntityActionPacket.Action.RELEASE_SHIFT_KEY));
      }

      resetSelected.run();

      // set the block place delay
      reflection.Minecraft_rightClickDelay.set(MC, cooldown.getValue());
    });
  }
}
