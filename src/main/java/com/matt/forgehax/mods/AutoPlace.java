package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getNetworkManager;
import static com.matt.forgehax.Helper.getPlayerController;
import static com.matt.forgehax.Helper.getWorld;
import static com.matt.forgehax.Helper.printInform;
import static com.matt.forgehax.Helper.printWarning;

import com.github.lunatrius.core.client.renderer.unique.GeometryMasks;
import com.github.lunatrius.core.client.renderer.unique.GeometryTessellator;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.matt.forgehax.asm.reflection.FastReflection.Fields;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.events.RenderEvent;
import com.matt.forgehax.mods.managers.PositionRotationManager;
import com.matt.forgehax.mods.managers.PositionRotationManager.RotationState.Local;
import com.matt.forgehax.util.BlockHelper;
import com.matt.forgehax.util.BlockHelper.BlockInfo;
import com.matt.forgehax.util.BlockHelper.BlockTraceInfo;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.color.Colors;
import com.matt.forgehax.util.command.Options;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.entity.LocalPlayerInventory;
import com.matt.forgehax.util.entity.LocalPlayerInventory.InvItem;
import com.matt.forgehax.util.entity.LocalPlayerUtils;
import com.matt.forgehax.util.entry.FacingEntry;
import com.matt.forgehax.util.key.BindingHelper;
import com.matt.forgehax.util.math.Angle;
import com.matt.forgehax.util.math.VectorUtils;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.matt.forgehax.util.serialization.ISerializableJson;
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
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.GL11;

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
          .defaults(() -> Collections.singleton(new FacingEntry(EnumFacing.UP)))
          .factory(FacingEntry::new)
          .supplier(Lists::newCopyOnWriteArrayList)
          .build();

  private final Setting<Boolean> use =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("use")
          .description("Will try to use the selected item on the target blocks instead of placing")
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

  private final Setting<Integer> cooldown =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("cooldown")
          .description("Block place delay to use after placing a block. Set to 0 to disable")
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

  private final Set<BlockPos> renderingBlocks = Sets.newConcurrentHashSet();
  private BlockPos currentRenderingTarget = null;

  private final KeyBinding bindSelect = new KeyBinding("AutoPlace Selection", -100, "ForgeHax");
  private final KeyBinding bindFinish = new KeyBinding("AutoPlace Finished", -98, "ForgeHax");

  private final AtomicBoolean bindSelectToggle = new AtomicBoolean(false);
  private final AtomicBoolean bindFinishToggle = new AtomicBoolean(false);
  private final AtomicBoolean printToggle = new AtomicBoolean(false);
  private final AtomicBoolean resetToggle = new AtomicBoolean(false);

  private final List<BlockInfo> targets = Lists.newArrayList();

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
      printInform("AutoPlace data has been reset.");
    }
  }

  private boolean isValidBlock(BlockInfo info) {
    return whitelist.get()
        ? targets.stream().anyMatch(info::equals)
        : targets.stream().noneMatch(info::equals);
  }

  private boolean isClickable(BlockInfo info) {
    return sides
        .stream()
        .map(FacingEntry::getFacing)
        .anyMatch(side -> BlockHelper.isBlockReplaceable(info.getPos().offset(side)));
  }

  private EnumFacing getBestFacingMatch(final String input) {
    return Arrays.stream(EnumFacing.values())
        .filter(side -> side.getName2().toLowerCase().contains(input.toLowerCase()))
        .min(
            Comparator.comparing(
                e -> e.getName2().toLowerCase(),
                Comparator.<String>comparingInt(
                        n -> StringUtils.getLevenshteinDistance(n, input.toLowerCase()))
                    .thenComparing(n -> n.startsWith(input))))
        .orElseGet(
            () -> {
              EnumFacing[] values = EnumFacing.values();
              try {
                int index = Integer.valueOf(input);
                return values[MathHelper.clamp(index, 0, values.length - 1)];
              } catch (NumberFormatException e) {
                return values[0];
              }
            });
  }

  private void showInfo(String filter) {
    MC.addScheduledTask(
        () -> {
          if ("selected".startsWith(filter))
            printInform(
                "Selected item %s",
                this.selectedItem.getItem().getRegistryName().toString()
                    + "{"
                    + this.selectedItem.getMetadata()
                    + "}");

          if ("targets".startsWith(filter))
            printInform(
                "Targets: %s",
                this.targets.stream().map(BlockInfo::toString).collect(Collectors.joining(", ")));

          if ("sides".startsWith(filter))
            printInform(
                "Sides: %s",
                this.sides
                    .stream()
                    .map(FacingEntry::getFacing)
                    .map(EnumFacing::getName2)
                    .collect(Collectors.joining(", ")));

          if ("whitelist".startsWith(filter))
            printInform("Whitelist: %s", Boolean.toString(whitelist.get()));

          if ("use".startsWith(filter)) printInform("Use: %s", Boolean.toString(use.get()));
        });
  }

  @Override
  protected void onLoad() {
    getCommandStub()
        .builders()
        .newCommandBuilder()
        .name("reset")
        .description("Reset to the setup process")
        .processor(
            data -> {
              resetToggle.set(true);
              if (getLocalPlayer() == null && getWorld() == null) reset();
            })
        .build();

    getCommandStub()
        .builders()
        .newCommandBuilder()
        .name("info")
        .description("Print info about the mod")
        .processor(
            data -> {
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
        .processor(
            data -> {
              final String name = data.getArgumentAsString(0);
              EnumFacing facing = getBestFacingMatch(name);

              if ("all".equalsIgnoreCase(name)) {
                sides.addAll(
                    Arrays.stream(EnumFacing.values())
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
        .processor(
            data -> {
              final String name = data.getArgumentAsString(0);
              EnumFacing facing = getBestFacingMatch(name);

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
        .processor(
            data -> {
              data.write(
                  "Sides: "
                      + sides
                          .stream()
                          .map(FacingEntry::getFacing)
                          .map(EnumFacing::getName2)
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
        .processor(
            data -> {
              String name = data.getArgumentAsString(0);

              if (config.get(name) == null) {
                PlaceConfigEntry entry = new PlaceConfigEntry(name);
                entry.setSides(
                    this.sides.stream().map(FacingEntry::getFacing).collect(Collectors.toSet()));
                entry.setTargets(this.targets);
                entry.setSelection(this.selectedItem);
                entry.setWhitelist(this.whitelist.get());
                entry.setUse(this.use.get());

                config.add(entry);
                config.serializeAll();
                data.write("Saved current config as " + name);
                data.markSuccess();
              } else {
                data.write(name + " is already in use!");
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
                      this.use.set(entry.isUse());

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
    if (!render.get() || MC.getRenderViewEntity() == null) return;

    Vec3d renderingOffset =
        EntityUtils.getInterpolatedPos(MC.getRenderViewEntity(), MC.getRenderPartialTicks());

    GlStateManager.pushMatrix();

    GlStateManager.disableTexture2D();
    GlStateManager.enableBlend();
    GlStateManager.disableAlpha();
    GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
    GlStateManager.shadeModel(GL11.GL_SMOOTH);
    GlStateManager.disableDepth();

    final GeometryTessellator tessellator = event.getTessellator();
    final BufferBuilder builder = tessellator.getBuffer();

    final double partialTicks = MC.getRenderPartialTicks();

    tessellator.beginLines();
    tessellator.setTranslation(0, 0, 0);

    GlStateManager.translate(0, 0, 0);
    GlStateManager.translate(-renderingOffset.x, -renderingOffset.y, -renderingOffset.z);

    renderingBlocks.forEach(
        pos -> {
          IBlockState state = getWorld().getBlockState(pos);
          AxisAlignedBB bb = state.getBoundingBox(getWorld(), pos);
          tessellator.setTranslation(pos.getX(), pos.getY(), pos.getZ());
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
    BlockPos current;
    try {
      current = new BlockPos(this.currentRenderingTarget);
    } catch (Throwable t) {
      current = null;
    }

    if (current != null) {
      IBlockState state = getWorld().getBlockState(current);
      AxisAlignedBB bb = state.getBoundingBox(getWorld(), current);
      tessellator.setTranslation(current.getX(), current.getY(), current.getZ());
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
    GlStateManager.enableAlpha();
    GlStateManager.enableTexture2D();
    GlStateManager.enableDepth();
    GlStateManager.enableCull();

    GL11.glDisable(GL11.GL_LINE_SMOOTH);
    GlStateManager.popMatrix();
  }

  @SubscribeEvent
  public void onUpdate(LocalPlayerUpdateEvent event) {
    reset();

    switch (stage) {
      case SELECT_BLOCKS:
        {
          if (printToggle.compareAndSet(false, true)) {
            targets.clear();
            printInform("Select blocks by pressing %s", BindingHelper.getIndexName(bindSelect));
            printInform("Finish this stage by pressing %s", BindingHelper.getIndexName(bindFinish));
          }

          if (bindSelect.isKeyDown() && bindSelectToggle.compareAndSet(false, true)) {
            RayTraceResult tr = LocalPlayerUtils.getMouseOverBlockTrace();
            if (tr == null) return;

            BlockInfo info = BlockHelper.newBlockInfo(tr.getBlockPos());

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
      case SELECT_REPLACEMENT:
        {
          if (printToggle.compareAndSet(false, true))
            printInform(
                "Hover over the block in your hot bar you want to place and press %s to select",
                BindingHelper.getIndexName(bindSelect));

          if (bindSelect.isKeyDown() && bindSelectToggle.compareAndSet(false, true)) {
            InvItem selected = LocalPlayerInventory.getSelected();

            if (selected.isNull()) {
              printWarning("No item selected!");
              return;
            }

            this.selectedItem =
                new ItemStack(selected.getItem(), 1, selected.getItemStack().getMetadata());

            printInform(
                "Selected item %s",
                this.selectedItem.getItem().getRegistryName().toString()
                    + "{"
                    + this.selectedItem.getMetadata()
                    + "}");

            stage = Stage.CONFIRM;
            printToggle.set(false);
          } else if (!bindSelect.isKeyDown()) {
            bindSelectToggle.set(false);
          }
          break;
        }
      case CONFIRM:
        {
          if (printToggle.compareAndSet(false, true)) {
            printInform(
                "Press %s to begin, or '.%s info' to set the current settings",
                BindingHelper.getIndexName(bindFinish), getModName());
          }

          if (bindFinish.isKeyDown()
              && selectedItem != null
              && bindFinishToggle.compareAndSet(false, true)) {
            printInform("Block place process started");
            printInform("Type '.%s reset' to restart the process", getModName());
            stage = Stage.READY;
          } else if (!bindFinish.isKeyDown()) {
            bindFinishToggle.set(false);
          }
          break;
        }
      case READY:
        {
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
    if (cooldown.get() > 0 && Fields.Minecraft_rightClickDelayTimer.get(MC) > 0) return;

    if (render.get()) {
      renderingBlocks.clear();
      currentRenderingTarget = null;
    }

    InvItem items =
        LocalPlayerInventory.getHotbarInventory()
            .stream()
            .filter(InvItem::nonNull)
            .filter(inv -> inv.getItem().equals(selectedItem.getItem()))
            .filter(
                item ->
                    !(item.getItem() instanceof ItemBlock)
                        || item.getItemStack().getMetadata() == selectedItem.getMetadata())
            .findFirst()
            .orElse(InvItem.EMPTY);

    if (items.isNull()) return;

    final Vec3d eyes = EntityUtils.getEyePos(getLocalPlayer());
    final Vec3d dir = LocalPlayerUtils.getViewAngles().getDirectionVector();

    List<BlockInfo> blocks =
        BlockHelper.getBlocksInRadius(eyes, getPlayerController().getBlockReachDistance())
            .stream()
            .map(BlockHelper::newBlockInfo)
            .filter(this::isValidBlock)
            .filter(this::isClickable)
            .sorted(
                Comparator.comparingDouble(
                    info ->
                        VectorUtils.getCrosshairDistance(
                            eyes, dir, BlockHelper.getOBBCenter(info.getPos()))))
            .collect(Collectors.toList());

    if (blocks.isEmpty()) return;

    if (render.get()) {
      currentRenderingTarget = null;
      renderingBlocks.clear();
      renderingBlocks.addAll(blocks.stream().map(BlockInfo::getPos).collect(Collectors.toSet()));
    }

    // find a block that can be placed
    int index = 0;
    BlockTraceInfo info = null;
    do {
      if (index >= blocks.size()) break;

      final BlockInfo at = blocks.get(index++);
      if (use.get()) {
        info =
            sides
                .stream()
                .map(FacingEntry::getFacing)
                .map(side -> BlockHelper.newBlockTrace(at.getPos(), side.getOpposite()))
                .filter(i -> BlockHelper.isTraceClear(eyes, i.getHitVec(), i.getSide()))
                .filter(i -> LocalPlayerUtils.isInReach(eyes, i.getHitVec()))
                .min(
                    Comparator.comparingDouble(
                        i -> VectorUtils.getCrosshairDistance(eyes, dir, i.getCenteredPos())))
                .orElse(null);
      } else {
        info =
            sides
                .stream()
                .map(FacingEntry::getFacing)
                .map(side -> BlockHelper.getBestBlockSide(at.getPos().offset(side)))
                .filter(Objects::nonNull)
                .findAny()
                .orElse(null);
      }
    } while (info == null);

    // if the block list is exhausted
    if (info == null) return;

    if (render.get()) currentRenderingTarget = info.getPos();

    Vec3d hit = info.getHitVec();
    Angle va = Utils.getLookAtAngles(hit);
    state.setServerAngles(va);

    final BlockTraceInfo blockInfo = info;
    state.invokeLater(
        rs -> {
          LocalPlayerInventory.setSelected(items);

          // enable sneaking to prevent unwanted block interactions
          boolean sneaking = LocalPlayerUtils.isSneaking();
          LocalPlayerUtils.setSneaking(true);

          getPlayerController()
              .processRightClickBlock(
                  getLocalPlayer(),
                  getWorld(),
                  blockInfo.getPos(),
                  blockInfo.getOppositeSide(),
                  hit,
                  EnumHand.MAIN_HAND);

          LocalPlayerUtils.setSneaking(sneaking);

          // stealth send swing packet
          getNetworkManager().sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));

          // set the block place delay
          Fields.Minecraft_rightClickDelayTimer.set(MC, cooldown.get());
        });
  }

  private static class PlaceConfigEntry implements ISerializableJson {
    private final String name;

    private final List<BlockInfo> targets = Lists.newArrayList();
    private final List<EnumFacing> sides = Lists.newArrayList();
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

    public List<BlockInfo> getTargets() {
      return Collections.unmodifiableList(targets);
    }

    public void setTargets(Collection<BlockInfo> list) {
      targets.clear();
      targets.addAll(
          list.stream()
              .filter(info -> !Blocks.AIR.equals(info.getBlock()))
              .collect(Collectors.toSet())); // collect to set to eliminate duplicates
    }

    public List<EnumFacing> getSides() {
      return Collections.unmodifiableList(sides);
    }

    public void setSides(Collection<EnumFacing> list) {
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
        writer.value(getSelection().getMetadata());
      }
      writer.endObject();

      writer.name("targets");
      writer.beginArray();
      {
        for (BlockInfo info : getTargets()) {
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
        for (EnumFacing side : getSides()) writer.value(side.getName2());
      }
      writer.endArray();

      writer.endObject();
    }

    @Override
    public void deserialize(JsonReader reader) throws IOException {
      reader.beginObject();

      while (reader.hasNext()) {
        switch (reader.nextName()) {
          case "selection":
            {
              reader.beginObject();

              reader.nextName();
              Item item = ItemSword.getByNameOrId(reader.nextString());

              reader.nextName();
              int meta = reader.nextInt();

              setSelection(new ItemStack(MoreObjects.firstNonNull(item, Items.AIR), 1, meta));

              reader.endObject();
              break;
            }
          case "targets":
            {
              reader.beginArray();

              List<BlockInfo> blocks = Lists.newArrayList();
              while (reader.hasNext()) {
                reader.beginObject();

                // block
                reader.nextName();
                Block block = Block.getBlockFromName(reader.nextString());

                // metadata
                reader.nextName();
                int meta = reader.nextInt();

                blocks.add(BlockHelper.newBlockInfo(block, meta));

                reader.endObject();
              }
              setTargets(blocks);

              reader.endArray();
              break;
            }
          case "use":
            {
              setUse(reader.nextBoolean());
              break;
            }
          case "whitelist":
            {
              setWhitelist(reader.nextBoolean());
              break;
            }
          case "sides":
            {
              reader.beginArray();

              List<EnumFacing> sides = Lists.newArrayList();
              while (reader.hasNext()) {
                sides.add(
                    Optional.ofNullable(reader.nextString())
                        .map(EnumFacing::byName)
                        .orElse(EnumFacing.UP));
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
