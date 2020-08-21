package dev.fiki.forgehax.main.mods.player;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.fiki.forgehax.main.managers.RotationManager;
import dev.fiki.forgehax.main.managers.RotationManager.RotationState.Local;
import dev.fiki.forgehax.main.services.HotbarSelectionService.ResetFunction;
import dev.fiki.forgehax.main.services.SneakService;
import dev.fiki.forgehax.main.util.BlockHelper;
import dev.fiki.forgehax.main.util.BlockHelper.BlockTraceInfo;
import dev.fiki.forgehax.main.util.BlockHelper.UniqueBlock;
import dev.fiki.forgehax.main.util.PacketHelper;
import dev.fiki.forgehax.main.util.Utils;
import dev.fiki.forgehax.main.util.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.main.util.cmd.settings.IntegerSetting;
import dev.fiki.forgehax.main.util.cmd.settings.KeyBindingSetting;
import dev.fiki.forgehax.main.util.cmd.settings.collections.SimpleSettingSet;
import dev.fiki.forgehax.main.util.color.Colors;
import dev.fiki.forgehax.main.util.draw.BufferBuilderEx;
import dev.fiki.forgehax.main.util.draw.GeometryMasks;
import dev.fiki.forgehax.main.util.entity.EntityUtils;
import dev.fiki.forgehax.main.util.entity.LocalPlayerInventory;
import dev.fiki.forgehax.main.util.entity.LocalPlayerUtils;
import dev.fiki.forgehax.main.util.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.events.RenderEvent;
import dev.fiki.forgehax.main.util.key.KeyConflictContexts;
import dev.fiki.forgehax.main.util.key.KeyInputs;
import dev.fiki.forgehax.main.util.math.Angle;
import dev.fiki.forgehax.main.util.math.VectorUtils;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import dev.fiki.forgehax.main.util.reflection.ReflectionTools;
import lombok.RequiredArgsConstructor;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static dev.fiki.forgehax.main.Common.*;

@RegisterMod(
    name = "AutoPlace",
    description = "Automatically place blocks on top of other blocks",
    category = Category.PLAYER
)
@RequiredArgsConstructor
public class AutoPlace extends ToggleMod implements RotationManager.MovementUpdateListener {
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
        .anyMatch(side -> BlockHelper.isBlockReplaceable(info.getPos().offset(side)));
  }

  private Direction getBestFacingMatch(final String input) {
    return Arrays.stream(Direction.values())
        .filter(side -> side.getName2().toLowerCase().contains(input.toLowerCase()))
        .min(Comparator.comparing(e -> e.getName2().toLowerCase(),
            Comparator.<String>comparingInt(n -> StringUtils.getLevenshteinDistance(n, input.toLowerCase()))
                .thenComparing(n -> n.startsWith(input))))
        .orElseGet(() -> {
          Direction[] values = Direction.values();
          try {
            int index = Integer.parseInt(input);
            return values[MathHelper.clamp(index, 0, values.length - 1)];
          } catch (NumberFormatException e) {
            return values[0];
          }
        });
  }

  private void showInfo(String filter) {
    addScheduledTask(() -> {
      if ("selected".startsWith(filter)) {
        printInform("Selected item %s",
            this.selectedItem.getDisplayName().getUnformattedComponentText());
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
                .map(Direction::getName2)
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
  protected void onEnabled() {
    RotationManager.getManager().register(this);
  }

  @Override
  protected void onDisabled() {
    RotationManager.getManager().unregister(this);
    printToggle.set(false);
  }

  @SubscribeEvent
  public void onRender(RenderEvent event) {
    if (!render.getValue() || MC.getRenderViewEntity() == null) {
      return;
    }

    RenderSystem.pushMatrix();

    RenderSystem.disableTexture();
    RenderSystem.enableBlend();
    RenderSystem.disableAlphaTest();
    RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
    RenderSystem.shadeModel(GL11.GL_SMOOTH);
    RenderSystem.disableDepthTest();

    final BufferBuilderEx builder = event.getBuffer();
    final Vector3d renderPos = EntityUtils.getInterpolatedPos(getLocalPlayer(), event.getPartialTicks());

    builder.beginLines(DefaultVertexFormats.POSITION_COLOR);

    renderingBlocks.forEach(pos -> {
          BlockState state = getWorld().getBlockState(pos);
          AxisAlignedBB bb = state.getCollisionShape(getWorld(), pos).getBoundingBox();
          builder.setTranslation(VectorUtils.toFPIVector(pos).subtract(renderPos));
          builder.putOutlinedCuboid(bb, GeometryMasks.Line.ALL, Colors.GREEN.setAlpha(150));
        });

    // poz
    final BlockPos current = this.currentRenderingTarget;

    if (current != null) {
      BlockState state = getWorld().getBlockState(current);
      AxisAlignedBB bb = state.getCollisionShape(getWorld(), current).getBoundingBox();
      builder.setTranslation(VectorUtils.toFPIVector(current).subtract(renderPos));
      builder.putOutlinedCuboid(bb, GeometryMasks.Line.ALL, Colors.RED.setAlpha(150));
    }

    builder.draw();

    RenderSystem.shadeModel(GL11.GL_FLAT);
    RenderSystem.disableBlend();
    RenderSystem.enableAlphaTest();
    RenderSystem.enableTexture();
    RenderSystem.enableDepthTest();
    RenderSystem.enableCull();

    GL11.glDisable(GL11.GL_LINE_SMOOTH);
    RenderSystem.popMatrix();
  }

  @SubscribeEvent
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
          BlockRayTraceResult tr = LocalPlayerUtils.getBlockViewTrace();
          if (RayTraceResult.Type.MISS.equals(tr.getType())) {
            return;
          }

          UniqueBlock info = BlockHelper.newUniqueBlock(tr.getPos());

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
          LocalPlayerInventory.InvItem selected = LocalPlayerInventory.getSelected();

          if (selected.isNull()) {
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

  @Override
  public void onLocalPlayerMovementUpdate(Local state) {
    if (!Stage.READY.equals(stage)) {
      renderingBlocks.clear();
      currentRenderingTarget = null;
      return;
    }
    if (cooldown.getValue() > 0 && reflection.Minecraft_rightClickDelayTimer.get(MC) > 0) {
      return;
    }

    if (render.getValue()) {
      renderingBlocks.clear();
      currentRenderingTarget = null;
    }

    LocalPlayerInventory.InvItem items = LocalPlayerInventory.getHotbarInventory().stream()
        .filter(LocalPlayerInventory.InvItem::nonNull)
        .filter(inv -> inv.getItem().equals(selectedItem.getItem()))
        .filter(inv -> BlockHelper.isItemBlockPlaceable(inv.getItem()))
        .findFirst()
        .orElse(LocalPlayerInventory.InvItem.EMPTY);

    if (items.isNull()) {
      return;
    }

    final Vector3d eyes = getLocalPlayer().getEyePosition(1.f);
    final Vector3d dir =
        client_angles.getValue()
            ? LocalPlayerUtils.getDirectionVector()
            : LocalPlayerUtils.getServerDirectionVector();

    List<UniqueBlock> blocks =
        BlockHelper.getBlocksInRadius(eyes, getPlayerController().getBlockReachDistance()).stream()
            .filter(pos -> !getWorld().isAirBlock(pos))
            .map(BlockHelper::newUniqueBlock)
            .filter(this::isValidBlock)
            .filter(this::isClickable)
            .sorted(Comparator.comparingDouble(info ->
                VectorUtils.getCrosshairDistance(eyes, dir, BlockHelper.getOBBCenter(info.getPos()))))
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
        trace =
            sides.stream()
                .map(side -> BlockHelper.getBlockSideTrace(eyes, at.getPos(), side))
                .filter(Objects::nonNull)
                .filter(tr -> tr.isPlaceable(items))
                .max(Comparator.comparing(BlockTraceInfo::isSneakRequired)
                    .thenComparing(i -> -VectorUtils.getCrosshairDistance(eyes, dir, i.getCenterPos())))
                .orElse(null);
      } else {
        trace =
            sides.stream()
                .map(side -> BlockHelper.getPlaceableBlockSideTrace(eyes, dir, at.getPos().offset(side)))
                .filter(Objects::nonNull)
                .filter(tr -> tr.isPlaceable(items))
                .max(Comparator.comparing(BlockTraceInfo::isSneakRequired)
                    .thenComparing(i -> -VectorUtils.getCrosshairDistance(eyes, dir, i.getCenterPos())))
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

    Angle va = Utils.getLookAtAngles(trace.getHitVec());
    state.setViewAngles(va, silent.getValue());

    final BlockTraceInfo tr = trace;
    state.invokeLater(rs -> {
      ResetFunction func = LocalPlayerInventory.setSelected(items);

      boolean sneak = tr.isSneakRequired() && !LocalPlayerUtils.isSneaking();
      if (sneak) {
        // send start sneaking packet
        PacketHelper.ignoreAndSend(new CEntityActionPacket(getLocalPlayer(),
            CEntityActionPacket.Action.PRESS_SHIFT_KEY));

        sneaks.setSuppressing(true);
        sneaks.setSneaking(true);
      }

      if (getPlayerController().func_217292_a(
          getLocalPlayer(),
          getWorld(),
          Hand.MAIN_HAND,
          new BlockRayTraceResult(tr.getHitVec(), tr.getOppositeSide(), tr.getPos(), false)).isSuccessOrConsume()) {
        // stealth send swing packet
        sendNetworkPacket(new CAnimateHandPacket(Hand.MAIN_HAND));
      }

      if (sneak) {
        sneaks.setSneaking(false);
        sneaks.setSuppressing(false);

        sendNetworkPacket(new CEntityActionPacket(getLocalPlayer(), CEntityActionPacket.Action.RELEASE_SHIFT_KEY));
      }

      func.revert();

      // set the block place delay
      reflection.Minecraft_rightClickDelayTimer.set(MC, cooldown.getValue());
    });
  }
}
