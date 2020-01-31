package dev.fiki.forgehax.main.mods;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.fiki.forgehax.main.Globals;
import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.events.RenderEvent;
import dev.fiki.forgehax.main.util.color.Colors;
import dev.fiki.forgehax.main.util.command.Options;
import dev.fiki.forgehax.main.util.command.Setting;
import dev.fiki.forgehax.main.util.entity.LocalPlayerInventory;
import dev.fiki.forgehax.main.util.entity.LocalPlayerUtils;
import dev.fiki.forgehax.main.util.entry.FacingEntry;
import dev.fiki.forgehax.main.util.math.Angle;
import dev.fiki.forgehax.main.util.math.VectorUtils;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.util.serialization.ISerializableJson;
import dev.fiki.forgehax.main.util.tesselation.GeometryMasks;
import dev.fiki.forgehax.main.util.tesselation.GeometryTessellator;
import dev.fiki.forgehax.main.mods.managers.PositionRotationManager;
import dev.fiki.forgehax.main.mods.managers.PositionRotationManager.RotationState.Local;
import dev.fiki.forgehax.main.mods.services.HotbarSelectionService.ResetFunction;
import dev.fiki.forgehax.main.util.BlockHelper;
import dev.fiki.forgehax.main.util.BlockHelper.BlockTraceInfo;
import dev.fiki.forgehax.main.util.BlockHelper.UniqueBlock;
import dev.fiki.forgehax.main.util.PacketHelper;
import dev.fiki.forgehax.main.util.Utils;
import dev.fiki.forgehax.main.util.key.BindingHelper;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.mojang.blaze3d.platform.GlStateManager;
import dev.fiki.forgehax.main.util.reflection.FastReflection;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.GL11;

import static dev.fiki.forgehax.main.Globals.*;

@RegisterMod
public class AutoPlace extends ToggleMod implements PositionRotationManager.MovementUpdateListener {
  
  enum Stage {
    SELECT_BLOCKS,
    SELECT_REPLACEMENT,
    CONFIRM,
    READY,
    ;
  }
  
  private final Options<PlaceConfigEntry> config =
      getCommandStub()
          .builders()
          .<PlaceConfigEntry>newOptionsBuilder()
          .name("config")
          .description("Saved selection configs")
          .factory(PlaceConfigEntry::new)
          .supplier(Lists::newCopyOnWriteArrayList)
          .build();
  
  private final Options<FacingEntry> sides =
      getCommandStub()
          .builders()
          .<FacingEntry>newOptionsBuilder()
          .name("sides")
          .description("Sides to place the blocks on")
          .defaults(() -> Collections.singleton(new FacingEntry(Direction.UP)))
          .factory(FacingEntry::new)
          .supplier(Lists::newCopyOnWriteArrayList)
          .build();
  
  private final Setting<Boolean> check_neighbors =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("check-neighbors")
          .description("Will check the neighboring blocks to see if a block can be placed")
          .defaultTo(false)
          .build();
  
  private final Setting<Boolean> whitelist =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("whitelist")
          .description("Makes the target list function as an inclusive list")
          .defaultTo(true)
          .build();
  
  private final Setting<Boolean> silent =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("silent")
          .description("Client angles don't change")
          .defaultTo(true)
          .build();
  
  private final Setting<Integer> cooldown =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("cooldown")
          .description(
              "Block place delay to check_neighbors after placing a block. Set to 0 to disable")
          .defaultTo(4)
          .min(0)
          .build();
  
  private final Setting<Boolean> render =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("render")
          .description("Show which blocks are currently visible and being targeted")
          .defaultTo(false)
          .changed(
              cb -> {
                if (cb.getTo()) {
                  this.renderingBlocks.clear();
                  this.currentRenderingTarget = null;
                }
              })
          .build();
  
  private final Setting<Boolean> client_angles =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("client-angles")
          .description("Sort the blocks to break by the clients angle instead of the servers")
          .defaultTo(false)
          .build();
  
  private final Set<BlockPos> renderingBlocks = Sets.newConcurrentHashSet();
  private BlockPos currentRenderingTarget = null;
  
  private final KeyBinding bindSelect = new KeyBinding("AutoPlace Selection", -100, "ForgeHax");
  private final KeyBinding bindFinish = new KeyBinding("AutoPlace Finished", -98, "ForgeHax");
  
  private final AtomicBoolean bindSelectToggle = new AtomicBoolean(false);
  private final AtomicBoolean bindFinishToggle = new AtomicBoolean(false);
  private final AtomicBoolean printToggle = new AtomicBoolean(false);
  private final AtomicBoolean resetToggle = new AtomicBoolean(false);
  
  private final List<UniqueBlock> targets = Lists.newArrayList();
  
  private ItemStack selectedItem = null;
  
  private Runnable resetTask = null;
  
  private Stage stage = Stage.SELECT_BLOCKS;
  
  public AutoPlace() {
    super(Category.PLAYER, "AutoPlace", false, "Automatically place blocks on top of other blocks");
    
    this.bindSelect.setKeyConflictContext(BindingHelper.getEmptyKeyConflictContext());
    this.bindFinish.setKeyConflictContext(BindingHelper.getEmptyKeyConflictContext());
    
    ClientRegistry.registerKeyBinding(this.bindSelect);
    ClientRegistry.registerKeyBinding(this.bindFinish);
  }
  
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
      Globals.printInform("AutoPlace data has been reset.");
    }
  }
  
  private boolean isValidBlock(UniqueBlock info) {
    return whitelist.get()
        ? targets.stream().anyMatch(info::equals)
        : targets.stream().noneMatch(info::equals);
  }
  
  private boolean isClickable(UniqueBlock info) {
    return sides
        .stream()
        .map(FacingEntry::getFacing)
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
    Globals.addScheduledTask(() -> {
      if ("selected".startsWith(filter)) {
        Globals.printInform("Selected item %s",
            this.selectedItem.getDisplayName().getUnformattedComponentText());
      }

      if ("targets".startsWith(filter)) {
        Globals.printInform(
            "Targets: %s",
            this.targets.stream().map(UniqueBlock::toString).collect(Collectors.joining(", ")));
      }

      if ("sides".startsWith(filter)) {
        Globals.printInform(
            "Sides: %s",
            this.sides
                .stream()
                .map(FacingEntry::getFacing)
                .map(Direction::getName2)
                .collect(Collectors.joining(", ")));
      }

      if ("whitelist".startsWith(filter)) {
        Globals.printInform("Whitelist: %s", Boolean.toString(whitelist.get()));
      }

      if ("check_neighbors".startsWith(filter)) {
        Globals.printInform("Check Neighbors: %s", Boolean.toString(check_neighbors.get()));
      }
    });
  }
  
  @Override
  protected void onLoad() {
    getCommandStub()
        .builders()
        .newCommandBuilder()
        .name("reset")
        .description("Reset to the setup process")
        .processor(data -> {
          resetToggle.set(true);
          if (Globals.getLocalPlayer() == null && Globals.getWorld() == null) {
            reset();
          }
        })
        .build();
    
    getCommandStub()
        .builders()
        .newCommandBuilder()
        .name("info")
        .description("Print info about the mod")
        .processor(data -> {
          String arg = data.getArgumentCount() > 1 ? data.getArgumentAsString(0) : "";
          showInfo(arg);
        })
        .build();
    
    sides
        .builders()
        .newCommandBuilder()
        .name("add")
        .description("Add side to the list")
        .requiredArgs(1)
        .processor(data -> {
          final String name = data.getArgumentAsString(0);
          Direction facing = getBestFacingMatch(name);

          if ("all".equalsIgnoreCase(name)) {
            sides.addAll(Arrays.stream(Direction.values())
                .map(FacingEntry::new)
                .filter(e -> !sides.contains(e))
                .collect(Collectors.toSet()));
            data.write("Added all sides");
            data.markSuccess();
            sides.serializeAll();
          } else if (sides.get(facing) == null) {
            sides.add(new FacingEntry(facing));
            data.write("Added side " + facing.getName2());
            data.markSuccess();
            sides.serializeAll();
          } else {
            data.write(facing.getName2() + " already exists");
            data.markFailed();
          }
        })
        .build();
    
    sides
        .builders()
        .newCommandBuilder()
        .name("remove")
        .description("Remove side from the list")
        .requiredArgs(1)
        .processor(data -> {
          final String name = data.getArgumentAsString(0);
          Direction facing = getBestFacingMatch(name);

          if ("all".equalsIgnoreCase(name)) {
            sides.clear();
            data.write("Removed all sides");
            data.markSuccess();
            sides.serializeAll();
          } else if (sides.remove(new FacingEntry(facing))) {
            data.write("Removed side " + facing.getName2());
            data.markSuccess();
            sides.serializeAll();
          } else {
            data.write(facing.getName2() + " doesn't exist");
            data.markFailed();
          }
        })
        .build();
    
    sides
        .builders()
        .newCommandBuilder()
        .name("list")
        .description("List all the current added sides")
        .processor(data -> {
          data.write("Sides: " + sides.stream()
              .map(FacingEntry::getFacing)
              .map(Direction::getName2)
              .collect(Collectors.joining(", ")));
          data.markSuccess();
        })
        .build();
    
    config
        .builders()
        .newCommandBuilder()
        .name("save")
        .description("Save current setup")
        .requiredArgs(1)
        .processor(data -> {
          String name = data.getArgumentAsString(0);
              
          if (config.get(name) == null) {
            PlaceConfigEntry entry = new PlaceConfigEntry(name);
            entry.setSides(this.sides.stream().map(FacingEntry::getFacing).collect(Collectors.toSet()));
            entry.setTargets(this.targets);
            entry.setSelection(this.selectedItem);
            entry.setWhitelist(this.whitelist.get());
            entry.setUse(this.check_neighbors.get());

            config.add(entry);
            config.serializeAll();
            data.write("Saved current config as " + name);
            data.markSuccess();
          } else {
            data.write(name + " is already in check_neighbors!");
            data.markFailed();
          }
        })
        .build();
    
    config
        .builders()
        .newCommandBuilder()
        .name("load")
        .description("Load config")
        .requiredArgs(1)
        .processor(
            data -> {
              String name = data.getArgumentAsString(0);
              
              PlaceConfigEntry entry = config.get(name);
              if (entry != null) {
                data.write(name + " loaded");
                resetTask =
                    () -> {
                      this.targets.clear();
                      this.targets.addAll(entry.getTargets());
                      
                      this.sides.clear();
                      this.sides.addAll(
                          entry
                              .getSides()
                              .stream()
                              .map(FacingEntry::new)
                              .collect(Collectors.toSet()));
                      
                      this.selectedItem = entry.getSelection();
                      
                      this.whitelist.set(entry.isWhitelist());
                      this.check_neighbors.set(entry.isUse());
                      
                      this.stage = Stage.CONFIRM;
                      
                      this.getCommandStub().serializeAll();
                    };
                this.resetToggle.set(true);
              } else {
                data.write(name + " doesn't exist!");
                data.markFailed();
              }
            })
        .build();
    
    config
        .builders()
        .newCommandBuilder()
        .name("delete")
        .description("Delete a configuration")
        .requiredArgs(1)
        .processor(
            data -> {
              String name = data.getArgumentAsString(0);
              
              if (config.remove(new PlaceConfigEntry(name))) {
                config.serializeAll();
                data.write("Deleted config " + name);
                data.markSuccess();
              } else {
                data.write(name + " doesn't exist!");
                data.markFailed();
              }
            })
        .build();
    
    config
        .builders()
        .newCommandBuilder()
        .name("list")
        .description("List all the current configs")
        .processor(
            data -> {
              data.write(
                  "Configs: "
                      + config
                      .stream()
                      .map(PlaceConfigEntry::getName)
                      .collect(Collectors.joining(", ")));
              data.markSuccess();
            })
        .build();
  }
  
  @Override
  protected void onEnabled() {
    PositionRotationManager.getManager().register(this);
  }
  
  @Override
  protected void onDisabled() {
    PositionRotationManager.getManager().unregister(this);
    printToggle.set(false);
  }
  
  @SubscribeEvent
  public void onRender(RenderEvent event) {
    if (!render.get() || Globals.MC.getRenderViewEntity() == null) {
      return;
    }
    
    GlStateManager.pushMatrix();
    
    GlStateManager.disableTexture();
    GlStateManager.enableBlend();
    GlStateManager.disableAlphaTest();
    GlStateManager.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
    GlStateManager.shadeModel(GL11.GL_SMOOTH);
    GlStateManager.disableDepthTest();
    
    final GeometryTessellator tessellator = event.getTessellator();
    final BufferBuilder builder = tessellator.getBuffer();
    
    tessellator.beginLines();
    tessellator.setTranslation(0, 0, 0);
    
    renderingBlocks.forEach(
        pos -> {
          BlockState state = Globals.getWorld().getBlockState(pos);
          AxisAlignedBB bb = state.getCollisionShape(Globals.getWorld(), pos).getBoundingBox();
          tessellator.setTranslation(
              (double) pos.getX() - event.getRenderPos().x,
              (double) pos.getY() - event.getRenderPos().y,
              (double) pos.getZ() - event.getRenderPos().z);
          GeometryTessellator.drawLines(
              builder,
              bb.minX,
              bb.minY,
              bb.minZ,
              bb.maxX,
              bb.maxY,
              bb.maxZ,
              GeometryMasks.Line.ALL,
              Colors.GREEN.setAlpha(150).toBuffer());
        });
    
    // poz
    final BlockPos current = this.currentRenderingTarget;
    
    if (current != null) {
      BlockState state = Globals.getWorld().getBlockState(current);
      AxisAlignedBB bb = state.getCollisionShape(Globals.getWorld(), current).getBoundingBox();
      tessellator.setTranslation(
          (double) current.getX() - event.getRenderPos().x,
          (double) current.getY() - event.getRenderPos().y,
          (double) current.getZ() - event.getRenderPos().z);
      GeometryTessellator.drawLines(
          builder,
          bb.minX,
          bb.minY,
          bb.minZ,
          bb.maxX,
          bb.maxY,
          bb.maxZ,
          GeometryMasks.Line.ALL,
          Colors.RED.setAlpha(150).toBuffer());
    }
    
    tessellator.draw();
    tessellator.setTranslation(0, 0, 0);
    
    GlStateManager.shadeModel(GL11.GL_FLAT);
    GlStateManager.disableBlend();
    GlStateManager.enableAlphaTest();
    GlStateManager.enableTexture();
    GlStateManager.enableDepthTest();
    GlStateManager.enableCull();
    
    GL11.glDisable(GL11.GL_LINE_SMOOTH);
    GlStateManager.popMatrix();
  }
  
  @SubscribeEvent
  public void onUpdate(LocalPlayerUpdateEvent event) {
    reset();
    
    switch (stage) {
      case SELECT_BLOCKS: {
        if (printToggle.compareAndSet(false, true)) {
          targets.clear();
          printInform("Select blocks by pressing %s", BindingHelper.getIndexName(bindSelect));
          printInform("Finish this stage by pressing %s", BindingHelper.getIndexName(bindFinish));
        }
        
        if (bindSelect.isKeyDown() && bindSelectToggle.compareAndSet(false, true)) {
          RayTraceResult tr = LocalPlayerUtils.getViewTrace();
          if (RayTraceResult.Type.MISS.equals(tr.getType())) {
            return;
          }
          
          UniqueBlock info = BlockHelper.newUniqueBlock(new BlockPos(tr.getHitVec()));
          
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
            Globals.printWarning("No items have been selected yet!");
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
              BindingHelper.getIndexName(bindSelect));
        }
        
        if (bindSelect.isKeyDown() && bindSelectToggle.compareAndSet(false, true)) {
          LocalPlayerInventory.InvItem selected = LocalPlayerInventory.getSelected();
          
          if (selected.isNull()) {
            Globals.printWarning("No item selected!");
            return;
          }
          
          this.selectedItem = new ItemStack(selected.getItem(), 1);
          
          Globals.printInform("Selected item %s", this.selectedItem.getItem().getRegistryName());
          
          stage = Stage.CONFIRM;
          printToggle.set(false);
        } else if (!bindSelect.isKeyDown()) {
          bindSelectToggle.set(false);
        }
        break;
      }
      case CONFIRM: {
        if (printToggle.compareAndSet(false, true)) {
          Globals.printInform(
              "Press %s to begin, or '.%s info' to set the current settings",
              BindingHelper.getIndexName(bindFinish), getModName());
        }
        
        if (bindFinish.isKeyDown()
            && selectedItem != null
            && bindFinishToggle.compareAndSet(false, true)) {
          Globals.printInform("Block place process started");
          Globals.printInform("Type '.%s reset' to restart the process", getModName());
          stage = Stage.READY;
        } else if (!bindFinish.isKeyDown()) {
          bindFinishToggle.set(false);
        }
        break;
      }
      case READY: {
        if (bindFinish.isKeyDown() && bindFinishToggle.compareAndSet(false, true)) {
          Globals.printInform("Block place process paused");
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
    if (cooldown.get() > 0 && FastReflection.Fields.Minecraft_rightClickDelayTimer.get(Globals.MC) > 0) {
      return;
    }
    
    if (render.get()) {
      renderingBlocks.clear();
      currentRenderingTarget = null;
    }
    
    LocalPlayerInventory.InvItem items = LocalPlayerInventory.getHotbarInventory().stream()
        .filter(LocalPlayerInventory.InvItem::nonNull)
        .filter(inv -> inv.getItem().equals(selectedItem.getItem()))
        // TODO: 1.15 find a way to find all placeable blocks
        .filter(item -> ItemGroup.BUILDING_BLOCKS.equals(item.getItem().getGroup()))
        .findFirst()
        .orElse(LocalPlayerInventory.InvItem.EMPTY);
    
    if (items.isNull()) {
      return;
    }
    
    final Vec3d eyes = LocalPlayerUtils.getEyePos();
    final Vec3d dir =
        client_angles.get()
            ? LocalPlayerUtils.getDirectionVector()
            : LocalPlayerUtils.getServerDirectionVector();
    
    List<UniqueBlock> blocks =
        BlockHelper.getBlocksInRadius(eyes, Globals.getPlayerController().getBlockReachDistance()).stream()
            .filter(pos -> !Globals.getWorld().isAirBlock(pos))
            .map(BlockHelper::newUniqueBlock)
            .filter(this::isValidBlock)
            .filter(this::isClickable)
            .sorted(
                Comparator.comparingDouble(
                    info ->
                        VectorUtils.getCrosshairDistance(
                            eyes, dir, BlockHelper.getOBBCenter(info.getPos()))))
            .collect(Collectors.toList());
    
    if (blocks.isEmpty()) {
      return;
    }
    
    if (render.get()) {
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
      if (!check_neighbors.get()) {
        trace =
            sides
                .stream()
                .map(FacingEntry::getFacing)
                .map(side -> BlockHelper.getBlockSideTrace(eyes, at.getPos(), side.getOpposite()))
                .filter(Objects::nonNull)
                .filter(tr -> tr.isPlaceable(items))
                .max(
                    Comparator.comparing(BlockTraceInfo::isSneakRequired)
                        .thenComparing(
                            i -> -VectorUtils.getCrosshairDistance(eyes, dir, i.getCenteredPos())))
                .orElse(null);
      } else {
        trace =
            sides
                .stream()
                .map(FacingEntry::getFacing)
                .map(
                    side ->
                        BlockHelper.getPlaceableBlockSideTrace(eyes, dir, at.getPos().offset(side)))
                .filter(Objects::nonNull)
                .filter(tr -> tr.isPlaceable(items))
                .max(
                    Comparator.comparing(BlockTraceInfo::isSneakRequired)
                        .thenComparing(
                            i -> -VectorUtils.getCrosshairDistance(eyes, dir, i.getCenteredPos())))
                .orElse(null);
      }
    } while (trace == null);
    
    // if the block list is exhausted
    if (trace == null) {
      return;
    }
    
    if (render.get()) {
      currentRenderingTarget = trace.getPos();
    }
    
    Angle va = Utils.getLookAtAngles(trace.getHitVec());
    state.setViewAngles(va, silent.get());
    
    final BlockTraceInfo tr = trace;
    state.invokeLater(
        rs -> {
          ResetFunction func = LocalPlayerInventory.setSelected(items);
          
          boolean sneak = tr.isSneakRequired() && !LocalPlayerUtils.isSneaking();
          if (sneak) {
            // send start sneaking packet
            PacketHelper.ignoreAndSend(new CEntityActionPacket(Globals.getLocalPlayer(),
                CEntityActionPacket.Action.PRESS_SHIFT_KEY));
            
            LocalPlayerUtils.setSneakingSuppression(true);
            LocalPlayerUtils.setSneaking(true);
          }
          
          Globals.getPlayerController().processRightClick(
                  Globals.getLocalPlayer(),
                  Globals.getWorld(),
                  // TODO: need to actually face the block
                  Hand.MAIN_HAND);
          
          // stealth send swing packet
          Globals.sendNetworkPacket(new CAnimateHandPacket(Hand.MAIN_HAND));
          
          if (sneak) {
            LocalPlayerUtils.setSneaking(false);
            LocalPlayerUtils.setSneakingSuppression(false);
            
            Globals.sendNetworkPacket(new CEntityActionPacket(Globals.getLocalPlayer(), CEntityActionPacket.Action.RELEASE_SHIFT_KEY));
          }
          
          func.revert();
          
          // set the block place delay
          FastReflection.Fields.Minecraft_rightClickDelayTimer.set(Globals.MC, cooldown.get());
        });
  }
  
  private static class PlaceConfigEntry implements ISerializableJson {
    
    private final String name;
    
    private final List<UniqueBlock> targets = Lists.newArrayList();
    private final List<Direction> sides = Lists.newArrayList();
    private ItemStack selection = ItemStack.EMPTY;
    private boolean use = false;
    private boolean whitelist = true;
    
    private PlaceConfigEntry(String name) {
      Objects.requireNonNull(name);
      this.name = name;
    }
    
    public String getName() {
      return name;
    }
    
    public List<UniqueBlock> getTargets() {
      return Collections.unmodifiableList(targets);
    }
    
    public void setTargets(Collection<UniqueBlock> list) {
      targets.clear();
      targets.addAll(
          list.stream()
              .filter(info -> !Blocks.AIR.equals(info.getBlock()))
              .collect(Collectors.toSet())); // collect to set to eliminate duplicates
    }
    
    public List<Direction> getSides() {
      return Collections.unmodifiableList(sides);
    }
    
    public void setSides(Collection<Direction> list) {
      sides.clear();
      sides.addAll(Sets.newLinkedHashSet(list)); // copy to set to eliminate duplicates
    }
    
    public ItemStack getSelection() {
      return selection;
    }
    
    public void setSelection(ItemStack selection) {
      this.selection =
          Optional.ofNullable(selection)
              .filter(s -> !Items.AIR.equals(s.getItem()))
              .orElse(ItemStack.EMPTY);
    }
    
    public boolean isUse() {
      return use;
    }
    
    public void setUse(boolean use) {
      this.use = use;
    }
    
    public boolean isWhitelist() {
      return whitelist;
    }
    
    public void setWhitelist(boolean whitelist) {
      this.whitelist = whitelist;
    }
    
    @Override
    public void serialize(JsonWriter writer) throws IOException {
      writer.beginObject();
      
      writer.name("selection");
      writer.beginObject();
      {
        writer.name("item");
        writer.value(getSelection().getItem().getRegistryName().toString());
        
        writer.name("metadata");
        writer.value(-1);
      }
      writer.endObject();
      
      writer.name("targets");
      writer.beginArray();
      {
        for (UniqueBlock info : getTargets()) {
          writer.beginObject();
          
          writer.name("block");
          writer.value(info.getBlock().getRegistryName().toString());
          
          writer.name("metadata");
          writer.value(info.getMetadata());
          
          writer.endObject();
        }
      }
      writer.endArray();
      
      writer.name("use");
      writer.value(isUse());
      
      writer.name("whitelist");
      writer.value(isWhitelist());
      
      writer.name("sides");
      writer.beginArray();
      {
        for (Direction side : getSides()) {
          writer.value(side.getName2());
        }
      }
      writer.endArray();
      
      writer.endObject();
    }
    
    @Override
    public void deserialize(JsonReader reader) throws IOException {
      reader.beginObject();
      
      while (reader.hasNext()) {
        switch (reader.nextName()) {
          case "selection": {
            reader.beginObject();
            
            reader.nextName();
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(reader.nextString()));
            
            reader.nextName();
            int meta = reader.nextInt();
            
            setSelection(new ItemStack(MoreObjects.firstNonNull(item, Items.AIR), 1));
            
            reader.endObject();
            break;
          }
          case "targets": {
            reader.beginArray();
            
            List<UniqueBlock> blocks = Lists.newArrayList();
            while (reader.hasNext()) {
              reader.beginObject();
              
              // block
              reader.nextName();
              Block block = Globals.getBlockRegistry().getValue(new ResourceLocation(reader.nextString()));
              
              // metadata
              reader.nextName();
              int meta = reader.nextInt();
              
              blocks.add(BlockHelper.newUniqueBlock(block, meta));
              
              reader.endObject();
            }
            setTargets(blocks);
            
            reader.endArray();
            break;
          }
          case "use": {
            setUse(reader.nextBoolean());
            break;
          }
          case "whitelist": {
            setWhitelist(reader.nextBoolean());
            break;
          }
          case "sides": {
            reader.beginArray();
            
            List<Direction> sides = Lists.newArrayList();
            while (reader.hasNext()) {
              sides.add(
                  Optional.ofNullable(reader.nextString())
                      .map(Direction::byName)
                      .orElse(Direction.UP));
            }
            setSides(sides);
            
            reader.endArray();
            break;
          }
          default:
            reader.skipValue();
            break;
        }
      }
      
      reader.endObject();
    }
    
    @Override
    public boolean equals(Object obj) {
      return this == obj
          || (obj instanceof PlaceConfigEntry
          && getName().equalsIgnoreCase(((PlaceConfigEntry) obj).getName()))
          || (obj instanceof String && getName().equalsIgnoreCase((String) obj));
    }
    
    @Override
    public String toString() {
      return name;
    }
  }
}
