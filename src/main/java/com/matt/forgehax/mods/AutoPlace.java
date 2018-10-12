package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getNetworkManager;
import static com.matt.forgehax.Helper.getPlayerController;
import static com.matt.forgehax.Helper.getWorld;
import static com.matt.forgehax.Helper.printInform;
import static com.matt.forgehax.Helper.printWarning;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.matt.forgehax.asm.reflection.FastReflection.Fields;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.mods.managers.PositionRotationManager;
import com.matt.forgehax.mods.managers.PositionRotationManager.RotationState.Local;
import com.matt.forgehax.util.BlockHelper;
import com.matt.forgehax.util.BlockHelper.BlockInfo;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.command.Options;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.entity.LocalPlayerInventory;
import com.matt.forgehax.util.entity.LocalPlayerInventory.InvItem;
import com.matt.forgehax.util.entity.LocalPlayerUtils;
import com.matt.forgehax.util.entity.LocalPlayerUtils.BlockPlacementInfo;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import net.minecraft.block.Block;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.StringUtils;

@RegisterMod
public class AutoPlace extends ToggleMod implements PositionRotationManager.MovementUpdateListener {
  enum Stage {
    SELECT_BLOCKS,
    SELECT_REPLACEMENT,
    READY,
    ;
  }

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

  private final Options<PlaceConfigEntry> config =
      getCommandStub()
          .builders()
          .<PlaceConfigEntry>newOptionsBuilder()
          .name("config")
          .description("Saved selection configs")
          .factory(PlaceConfigEntry::new)
          .supplier(Lists::newCopyOnWriteArrayList)
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

  private final Setting<Boolean> use =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("use")
          .description("Will try to use the selected item on the target blocks instead of placing")
          .defaultTo(false)
          .build();

  private final KeyBinding selection = new KeyBinding("AutoPlace Selection", -100, "ForgeHax");;
  private final KeyBinding finished = new KeyBinding("AutoPlace Finished", -98, "ForgeHax");

  private final AtomicBoolean selectionToggle = new AtomicBoolean(false);
  private final AtomicBoolean finishedToggle = new AtomicBoolean(false);
  private final AtomicBoolean printOnce = new AtomicBoolean(false);
  private final AtomicBoolean reset = new AtomicBoolean(false);

  private final List<BlockInfo> targets = Lists.newArrayList();
  private ItemStack selectedItem = null;

  private Runnable resetTask = null;

  private Stage stage = Stage.SELECT_BLOCKS;

  public AutoPlace() {
    super(Category.PLAYER, "AutoPlace", false, "Automatically place blocks on top of other blocks");

    this.selection.setKeyConflictContext(BindingHelper.getEmptyKeyConflictContext());
    this.finished.setKeyConflictContext(BindingHelper.getEmptyKeyConflictContext());

    ClientRegistry.registerKeyBinding(this.selection);
    ClientRegistry.registerKeyBinding(this.finished);
  }

  private void reset() {
    if (reset.compareAndSet(true, false)) {
      targets.clear();
      selectedItem = null;
      stage = Stage.SELECT_BLOCKS;
      printOnce.set(false);
      if (resetTask != null) {
        resetTask.run();
        resetTask = null;
      }
      printInform("AutoPlace data has been reset.");
    }
  }

  private boolean canRightClick(BlockInfo info) {
    return use.get()
        || sides
            .stream()
            .map(FacingEntry::getFacing)
            .anyMatch(side -> BlockHelper.isBlockReplaceable(info.getPos().offset(side)));
  }

  private EnumFacing getBestFacingMatch(final String name) {
    return Arrays.stream(EnumFacing.values())
        .filter(side -> side.getName2().toLowerCase().contains(name.toLowerCase()))
        .min(
            Comparator.comparing(
                e ->
                    StringUtils.getLevenshteinDistance(
                        e.getName2().toLowerCase(), name.toLowerCase())))
        .orElseGet(
            () -> {
              EnumFacing[] values = EnumFacing.values();
              try {
                int index = Integer.valueOf(name);
                return values[MathHelper.clamp(index, 0, values.length - 1)];
              } catch (NumberFormatException e) {
                return values[0];
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
        .processor(
            data -> {
              reset.set(true);
              if (getLocalPlayer() == null && getWorld() == null) reset();
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

              if (sides.get(facing) == null) {
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

              if (sides.remove(new FacingEntry(facing))) {
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
                entry.setSelection(selectedItem);
                entry.setTargets(targets);
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
                      this.selectedItem = entry.getSelection();
                      this.stage = Stage.READY;
                    };
                this.reset.set(true);
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
    printOnce.set(false);
  }

  @SubscribeEvent
  public void onUpdate(LocalPlayerUpdateEvent event) {
    reset();

    switch (stage) {
      case READY:
        return;
      case SELECT_BLOCKS:
        {
          if (printOnce.compareAndSet(false, true)) {
            targets.clear();
            printInform("Select blocks by pressing %s", BindingHelper.getIndexName(selection));
            printInform("Finish this stage by pressing %s", BindingHelper.getIndexName(finished));
          }

          if (selection.isKeyDown() && selectionToggle.compareAndSet(false, true)) {
            RayTraceResult tr = LocalPlayerUtils.getMouseOverBlockTrace();
            if (tr == null) return;

            BlockInfo info = BlockHelper.newBlockInfo(tr.getBlockPos());
            if (!targets.contains(info)) {
              printInform("Added block %s", info.toString());
              targets.add(info);
            } else {
              printInform("Removed block %s", info.toString());
              targets.remove(info);
            }
          } else {
            selectionToggle.set(false);
          }

          if (finished.isKeyDown() && finishedToggle.compareAndSet(false, true)) {
            if (targets.isEmpty()) {
              printWarning("No items have been selected yet!");
            } else {
              stage = Stage.SELECT_REPLACEMENT;
              printOnce.set(false);
            }
          } else {
            finishedToggle.set(false);
          }
          break;
        }
      case SELECT_REPLACEMENT:
        {
          if (printOnce.compareAndSet(false, true))
            printInform(
                "Hover over the block in your hot bar you want to place and press %s to select",
                BindingHelper.getIndexName(selection));

          if (selection.isKeyDown() && selectionToggle.compareAndSet(false, true)) {
            InvItem selected = LocalPlayerInventory.getSelected();

            if (selected.isNull()) {
              printWarning("No item selected!");
              return;
            }

            this.selectedItem =
                new ItemStack(selected.getItem(), 1, selected.getItemStack().getMetadata());

            printInform("Selected %s", this.selectedItem.getDisplayName());
            printInform("Press %s to begin", BindingHelper.getIndexName(finished));
          } else {
            selectionToggle.set(false);
          }

          if (finished.isKeyDown()
              && selectedItem != null
              && finishedToggle.compareAndSet(false, true)) {
            stage = Stage.READY;
            printInform("Block place process started");
            printInform("Type '.%s reset' to restart the process", getModName());
          } else {
            finishedToggle.set(false);
          }
          break;
        }
    }
  }

  @Override
  public void onLocalPlayerMovementUpdate(Local state) {
    if (!Stage.READY.equals(stage)) return;
    if (cooldown.get() > 0 && Fields.Minecraft_rightClickDelayTimer.get(MC) > 0) return;

    InvItem items =
        LocalPlayerInventory.getHotbarInventory()
            .stream()
            .filter(InvItem::nonNull)
            .filter(inv -> inv.getItemStack().isItemEqual(selectedItem))
            .filter(item -> item.getItemStack().getMetadata() == selectedItem.getMetadata())
            .findFirst()
            .orElse(InvItem.EMPTY);

    if (items.isNull()) return;

    final Vec3d eyes = EntityUtils.getEyePos(getLocalPlayer());
    final Vec3d dir = LocalPlayerUtils.getViewAngles().getDirectionVector();

    List<BlockInfo> blocks =
        BlockHelper.getBlocksInRadius(eyes, getPlayerController().getBlockReachDistance())
            .stream()
            .map(BlockHelper::newBlockInfo)
            .filter(info -> targets.stream().anyMatch(info::equals))
            .filter(this::canRightClick)
            .sorted(
                Comparator.comparingDouble(
                    info -> VectorUtils.getCrosshairDistance(eyes, dir, new Vec3d(info.getPos()))))
            .collect(Collectors.toList());

    if (blocks.isEmpty()) return;

    // find a block that can be placed
    int index = 0;
    BlockPlacementInfo info = null;
    do {
      if (index >= blocks.size()) break;

      final BlockInfo at = blocks.get(index++);
      if (use.get()) info = LocalPlayerUtils.getBlockPlacementInfo(at.getPos());
      else
        info =
            sides
                .stream()
                .map(FacingEntry::getFacing)
                .map(side -> LocalPlayerUtils.getBlockPlacementInfo(at.getPos().offset(side)))
                .filter(Objects::nonNull)
                .findAny()
                .orElse(null);
    } while (info == null);

    // if the block list is exhausted
    if (info == null) return;

    Vec3d hit = info.getHitVec();
    Angle va = Utils.getLookAtAngles(hit);
    state.setServerAngles(va);

    final BlockPlacementInfo blockInfo = info;
    state.invokeLater(
        rs -> {
          LocalPlayerInventory.setSelected(items);
          getPlayerController()
              .processRightClickBlock(
                  getLocalPlayer(),
                  getWorld(),
                  blockInfo.getPos(),
                  blockInfo.getOppositeSide(),
                  hit,
                  EnumHand.MAIN_HAND);
          getNetworkManager().sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
          Fields.Minecraft_rightClickDelayTimer.set(MC, cooldown.get());
        });
  }

  private static class PlaceConfigEntry implements ISerializableJson {
    private final String name;

    private final List<BlockInfo> targets = Lists.newArrayList();
    private ItemStack selection = ItemStack.EMPTY;

    public PlaceConfigEntry(String name) {
      Objects.requireNonNull(name);
      this.name = name;
    }

    public String getName() {
      return name;
    }

    public List<BlockInfo> getTargets() {
      return Collections.unmodifiableList(targets);
    }

    public ItemStack getSelection() {
      return selection;
    }

    public void setTargets(Collection<BlockInfo> list) {
      targets.addAll(list);
    }

    public void setSelection(ItemStack selection) {
      this.selection = selection;
    }

    @Override
    public void serialize(JsonWriter writer) throws IOException {
      writer.beginObject();

      writer.name("selection");
      writer.beginObject();
      {
        writer.name("item");
        writer.value(selection.getItem().getRegistryName().toString());

        writer.name("metadata");
        writer.value(selection.getMetadata());
      }
      writer.endObject();

      writer.name("targets");
      writer.beginArray();
      {
        for (BlockInfo info : targets) {
          writer.beginObject();

          writer.name("block");
          writer.value(info.getBlock().getRegistryName().toString());

          writer.name("metadata");
          writer.value(info.getMetadata());

          writer.endObject();
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
          case "selection":
            {
              reader.beginObject();

              reader.nextName();
              Item item = ItemSword.getByNameOrId(reader.nextString());

              reader.nextName();
              int meta = reader.nextInt();

              selection = new ItemStack(MoreObjects.firstNonNull(item, Items.AIR), 1, meta);

              reader.endObject();
              break;
            }
          case "targets":
            {
              targets.clear();

              reader.beginArray();

              while (reader.hasNext()) {
                reader.beginObject();

                reader.nextName();
                Block block = Block.getBlockFromName(reader.nextString());

                reader.nextName();
                int meta = reader.nextInt();

                targets.add(BlockHelper.newBlockInfo(block, meta));

                reader.endObject();
              }

              reader.endArray();
              break;
            }
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
