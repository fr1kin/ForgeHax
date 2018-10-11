package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getNetworkManager;
import static com.matt.forgehax.Helper.getWorld;
import static com.matt.forgehax.Helper.printMessage;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.matt.forgehax.asm.reflection.FastReflection.Fields;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.mods.managers.PositionRotationManager;
import com.matt.forgehax.mods.managers.PositionRotationManager.RotationState.Local;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.entity.LocalPlayerInventory;
import com.matt.forgehax.util.entity.LocalPlayerInventory.InvItem;
import com.matt.forgehax.util.entity.LocalPlayerUtils;
import com.matt.forgehax.util.entity.LocalPlayerUtils.BlockPlacementInfo;
import com.matt.forgehax.util.math.Angle;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

@RegisterMod
public class AutoPlace extends ToggleMod implements PositionRotationManager.MovementUpdateListener {
  private static final Map<Integer, String> MOUSE_CODES = Maps.newHashMap();

  static {
    MOUSE_CODES.put(-100, "MOUSE_LEFT");
    MOUSE_CODES.put(-99, "MOUSE_RIGHT");
    MOUSE_CODES.put(-98, "MOUSE_MIDDLE");
  }

  private static String getKeyCodeName(int code) {
    if (MOUSE_CODES.get(code) != null) return MOUSE_CODES.get(code);
    else if (code < 0) return Mouse.getButtonName(100 + code);
    else return Keyboard.getKeyName(code);
  }

  enum Stage {
    SELECT_BLOCKS,
    SELECT_REPLACEMENT,
    READY,
    ;
  }

  private final KeyBinding selection;
  private final KeyBinding finished;

  private final List<BlockInfo> targeting = Lists.newArrayList();
  private BlockInfo replacement = null;

  private Stage stage = Stage.SELECT_BLOCKS;

  private boolean once = false;
  private boolean reset = false;

  private final Setting<Integer> place_delay =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("place-delay")
          .description("Block place delay to use after placing a block. Set to 0 to disable")
          .defaultTo(4)
          .min(0)
          .build();

  public AutoPlace() {
    super(Category.PLAYER, "AutoPlace", false, "Automatically place blocks on top of other blocks");

    IKeyConflictContext context =
        new IKeyConflictContext() {
          @Override
          public boolean isActive() {
            return false;
          }

          @Override
          public boolean conflicts(IKeyConflictContext other) {
            return false;
          }
        };

    // https://minecraft.gamepedia.com/Key_codes#Mouse_codes
    this.selection = new KeyBinding("AutoPlace Selection", context, -100, "ForgeHax");
    this.finished = new KeyBinding("AutoPlace Finished", context, -98, "ForgeHax");

    ClientRegistry.registerKeyBinding(this.selection);
    ClientRegistry.registerKeyBinding(this.finished);
  }

  private List<BlockPos> getBlocksInRadius(Vec3d pos, double radius) {
    List<BlockPos> list = Lists.newArrayList();
    for (double x = pos.x - radius; x <= pos.x + radius; ++x) {
      for (double y = pos.y - radius; y <= pos.y + radius; ++y) {
        for (double z = pos.z - radius; z <= pos.z + radius; ++z) {
          list.add(new BlockPos((int) x, (int) y, (int) z));
        }
      }
    }
    return list;
  }

  @Override
  protected void onLoad() {
    getCommandStub()
        .builders()
        .newCommandBuilder()
        .name("reset")
        .description("Reset to the setup process")
        .processor(data -> reset = true)
        .build();
  }

  @Override
  protected void onEnabled() {
    PositionRotationManager.getManager().register(this);
  }

  @Override
  protected void onDisabled() {
    PositionRotationManager.getManager().unregister(this);
  }

  @SubscribeEvent
  public void onUpdate(LocalPlayerUpdateEvent event) {
    if (reset) {
      targeting.clear();
      replacement = null;
      stage = Stage.SELECT_BLOCKS;
      once = false;
      reset = false;
      printMessage("AutoPlace data has been reset.");
    }

    switch (stage) {
      case READY:
        return;
      case SELECT_BLOCKS:
        {
          if (!once) {
            targeting.clear();
            printMessage(
                String.format(
                    "Select blocks by pressing %s", getKeyCodeName(selection.getKeyCode())));
            printMessage(
                String.format(
                    "Finish this stage by pressing %s", getKeyCodeName(finished.getKeyCode())));
            once = true;
          }

          int time = Fields.Binding_pressTime.get(selection);
          if (selection.isKeyDown() && time == 0) {
            Fields.Binding_pressTime.set(selection, ++time);
            RayTraceResult tr = LocalPlayerUtils.getMouseOverBlockTrace();
            if (tr == null) return;

            IBlockState state = getWorld().getBlockState(tr.getBlockPos());
            BlockInfo info =
                new BlockInfo(state.getBlock(), state.getBlock().getMetaFromState(state));

            if (!targeting.contains(info)) {
              printMessage(
                  String.format(
                      "Added block %s",
                      new ItemStack(info.getBlock(), 1, info.getMetadata()).getDisplayName()));
              targeting.add(info);
            } else {
              printMessage(
                  String.format(
                      "Removed block %s",
                      new ItemStack(info.getBlock(), 1, info.getMetadata()).getDisplayName()));
              targeting.remove(info);
            }
          } else {
            Fields.Binding_pressTime.set(selection, 0);
          }

          time = Fields.Binding_pressTime.get(finished);
          if (finished.isKeyDown() && time == 0) {
            Fields.Binding_pressTime.set(finished, ++time);
            if (targeting.isEmpty()) {
              printMessage("No items have been selected yet!");
            } else {
              stage = Stage.SELECT_REPLACEMENT;
              once = false;
            }
          } else {
            Fields.Binding_pressTime.set(finished, 0);
          }
          break;
        }
      case SELECT_REPLACEMENT:
        {
          if (!once) {
            printMessage(
                String.format(
                    "Hover over the block in your hot bar you want to place and press %s to select",
                    getKeyCodeName(selection.getKeyCode())));
            once = true;
          }

          int time = Fields.Binding_pressTime.get(selection);
          if (selection.isKeyDown() && time == 0) {
            Fields.Binding_pressTime.set(selection, ++time);

            InvItem selected = LocalPlayerInventory.getSelected();

            if (selected.isNull()) {
              printMessage("No item selected! Try again.");
              return;
            }

            if (!(selected.getItem() instanceof ItemBlock)) {
              printMessage("Selection must be a block type!");
              return;
            }

            replacement =
                new BlockInfo(
                    Block.getBlockFromItem(selected.getItem()),
                    selected.getItemStack().getMetadata());
            printMessage(
                "Selected "
                    + new ItemStack(replacement.getBlock(), 1, replacement.getMetadata())
                        .getDisplayName());
            printMessage(
                String.format("Press %s to begin.", getKeyCodeName(finished.getKeyCode())));
          } else {
            Fields.Binding_pressTime.set(selection, 0);
          }

          time = Fields.Binding_pressTime.get(finished);
          if (finished.isKeyDown() && time == 0 && replacement != null) {
            Fields.Binding_pressTime.set(finished, ++time);
            stage = Stage.READY;
            printMessage("Block place process started.");
            printMessage(String.format("Type '.%s reset' to restart the process.", getModName()));
          } else {
            Fields.Binding_pressTime.set(finished, 0);
          }
          break;
        }
    }
  }

  @Override
  public void onLocalPlayerMovementUpdate(Local state) {
    if (!Stage.READY.equals(stage)) return;

    if (place_delay.get() != 0 && Fields.Minecraft_rightClickDelayTimer.get(MC) > 0) return;

    InvItem items =
        LocalPlayerInventory.getHotbarInventory()
            .stream()
            .filter(InvItem::nonNull)
            .filter(item -> item.getItem() instanceof ItemBlock)
            .filter(
                item ->
                    Objects.equals(replacement.getBlock(), Block.getBlockFromItem(item.getItem())))
            .filter(item -> replacement.getMetadata() == item.getItemStack().getMetadata())
            .findFirst()
            .orElse(InvItem.EMPTY);

    if (items.isNull()) return;

    LocalPlayerInventory.setSelected(items);

    final Vec3d eyes = EntityUtils.getEyePos(getLocalPlayer());
    final Vec3d dir = LocalPlayerUtils.getViewAngles().getDirectionVector();

    List<BlockInfo> blocks =
        getBlocksInRadius(
                getLocalPlayer().getPositionVector(), MC.playerController.getBlockReachDistance())
            .stream()
            .map(BlockInfo::new)
            .filter(info -> targeting.stream().anyMatch(info::equals))
            .filter(
                info -> getWorld().getBlockState(info.getPos().up()).getMaterial().isReplaceable())
            .sorted(
                Comparator.comparingDouble(
                    info ->
                        new Vec3d(info.getPos())
                            .subtract(eyes)
                            .normalize()
                            .subtract(dir)
                            .lengthSquared()))
            .collect(Collectors.toList());

    if (blocks.isEmpty()) return;

    // find a block that can be placed
    int index = 0;
    BlockPlacementInfo info = null;
    do {
      if (index >= blocks.size()) break;

      info = LocalPlayerUtils.getBlockAroundPlacementInfo(blocks.get(index++).getPos().up());
    } while (info == null);

    // if the block list is exhausted
    if (info == null) return;

    Vec3d hit = info.getHitVec();
    Angle va = Utils.getLookAtAngles(hit);
    state.setServerAngles(va);

    final BlockPlacementInfo blockInfo = info;
    state.invokeLater(
        rs -> {
          MC.playerController.processRightClickBlock(
              getLocalPlayer(),
              getWorld(),
              blockInfo.getPos(),
              blockInfo.getOppositeSide(),
              hit,
              EnumHand.MAIN_HAND);
          getNetworkManager().sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
          Fields.Minecraft_rightClickDelayTimer.set(MC, place_delay.get());
        });
  }

  private static class BlockInfo {
    private final Block block;
    private final int metadata;
    private final BlockPos pos;

    public BlockInfo(Block block, int metadata) {
      this.block = block;
      this.metadata = metadata;
      this.pos = BlockPos.ORIGIN;
    }

    public BlockInfo(BlockPos pos) {
      IBlockState state = getWorld().getBlockState(pos);
      this.block = state.getBlock();
      this.metadata = this.block.getMetaFromState(state);
      this.pos = pos;
    }

    public Block getBlock() {
      return block;
    }

    public int getMetadata() {
      return metadata;
    }

    public BlockPos getPos() {
      return pos;
    }

    public boolean isEqual(BlockPos pos) {
      IBlockState state = getWorld().getBlockState(pos);
      Block bl = state.getBlock();
      return Objects.equals(getBlock(), bl) && getMetadata() == bl.getMetaFromState(state);
    }

    @Override
    public boolean equals(Object obj) {
      return this == obj
          || (obj instanceof BlockInfo
              && getBlock().equals(((BlockInfo) obj).getBlock())
              && getMetadata() == ((BlockInfo) obj).getMetadata());
    }
  }
}
