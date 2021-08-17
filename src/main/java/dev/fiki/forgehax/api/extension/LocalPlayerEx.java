package dev.fiki.forgehax.api.extension;

import com.google.common.base.MoreObjects;
import dev.fiki.forgehax.api.Switch;
import dev.fiki.forgehax.api.entity.HeldSlot;
import dev.fiki.forgehax.api.math.Angle;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import net.minecraft.block.Block;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

import static dev.fiki.forgehax.main.Common.*;

/**
 * Class for dealing with the local player only
 */
public class LocalPlayerEx {
  @Getter
  private static final SelectedItemData selectedItemData = new SelectedItemData();

  @Getter
  @Setter
  private static Angle serverAngles = null;

  public static ClientWorld getWorld(ClientPlayerEntity lp) {
    return lp.clientLevel;
  }

  /**
   * Gets the players current view angles
   */
  public static Angle getViewAngles(ClientPlayerEntity lp) {
    return lp == null ? Angle.ZERO : Angle.degrees(lp.xRot, lp.yRot);
  }

  public static void setViewAngles(ClientPlayerEntity lp, Angle angles) {
    Angle original = getViewAngles(lp);
    Angle diff = angles.normalize().sub(original.normalize()).normalize();
    lp.xRot = GeneralEx.clamp(original.getPitch() + diff.getPitch(), -90.f, 90.f);
    lp.yRot = original.getYaw() + diff.getYaw();
  }

  public static void setViewAngles(ClientPlayerEntity lp, float pitch, float yaw) {
    setViewAngles(lp, Angle.degrees(pitch, yaw));
  }

  public static void setViewAnglesRaw(ClientPlayerEntity lp, float pitch, float yaw) {
    lp.xRot = pitch;
    lp.yRot = yaw;
  }

  public static Angle getServerAngles(@Nullable ClientPlayerEntity lp) {
    return serverAngles == null ? Angle.ZERO : serverAngles;
  }

  public static Vector3d getVelocity(ClientPlayerEntity lp) {
    return lp.getDeltaMovement();
  }

  public static boolean isCrouchSneaking(ClientPlayerEntity lp) {
    return lp.isCrouching();
  }

  public static boolean setCrouchSneaking(ClientPlayerEntity lp, boolean sneak) {
    boolean old = isCrouchSneaking(lp);
    lp.setShiftKeyDown(sneak);
    if (lp.input != null) {
      lp.input.shiftKeyDown = sneak;
    }
    return old;
  }

  public static Vector3d getDirectionVector(ClientPlayerEntity lp) {
    return getViewAngles(lp).getDirectionVector().normalize();
  }

  public static Vector3d getServerDirectionVector(ClientPlayerEntity lp) {
    return getServerAngles(lp).getDirectionVector().normalize();
  }

  public static RayTraceResult getViewTrace(@Nullable ClientPlayerEntity lp) {
    return MC.hitResult;
  }

  public static BlockRayTraceResult getBlockViewTrace(ClientPlayerEntity lp) {
    Vector3d start = EntityEx.getEyePos(lp);
    Vector3d end = start.add(lp.getViewVector(1.f).normalize().scale(getBlockReach(lp)));
    RayTraceContext ctx = new RayTraceContext(start, end, RayTraceContext.BlockMode.OUTLINE,
        RayTraceContext.FluidMode.NONE, lp);
    return getWorld(lp).clip(ctx);
  }

  public static double getBlockReach(@Nullable ClientPlayerEntity lp) {
    return getPlayerController().getPickRange();
  }

  public static boolean isInReach(@Nullable ClientPlayerEntity lp, Vector3d start, Vector3d end) {
    return start.distanceToSqr(end) < getBlockReach(lp) * getBlockReach(lp);
  }

  public static ActionResultType placeBlock(ClientPlayerEntity lp, Hand hand, BlockRayTraceResult tr) {
    return getPlayerController().useItemOn(lp, getWorld(lp), hand, tr);
  }

  public static ActionResultType rightClick(ClientPlayerEntity lp, Hand hand) {
    return getPlayerController().useItem(lp, getWorld(lp), hand);
  }

  public static void attackEntity(ClientPlayerEntity lp, Entity target) {
    getPlayerController().attack(lp, target);
  }

  public static void swingHandSilently(@Nullable ClientPlayerEntity lp) {
    GeneralEx.dispatchNetworkPacket(getNetworkManager(), new CAnimateHandPacket(Hand.MAIN_HAND));
  }

  public static void swingOffhandSilently(@Nullable ClientPlayerEntity lp) {
    GeneralEx.dispatchNetworkPacket(getNetworkManager(), new CAnimateHandPacket(Hand.OFF_HAND));
  }

  public static PlayerInventory getInventory(ClientPlayerEntity lp) {
    return lp.inventory;
  }

  public static PlayerContainer getContainer(ClientPlayerEntity lp) {
    return lp.inventoryMenu;
  }

  public static Container getOpenContainer(ClientPlayerEntity lp) {
    return lp.containerMenu;
  }

  public static Container getCurrentContainer(ClientPlayerEntity lp) {
    Container oc = getOpenContainer(lp);
    return oc != null ? oc : getContainer(lp);
  }

  public static int getHotbarSize(@Nullable ClientPlayerEntity lp) {
    return PlayerInventory.getSelectionSize();
  }

  public static List<ItemStack> getMainInventory(ClientPlayerEntity lp) {
    return getInventory(lp).items;
  }

  /**
   * Get all of the slots in the players inventory
   * @param lp local player object
   * @return list of container slots
   */
  public static List<Slot> getSlots(ClientPlayerEntity lp) {
    return getContainer(lp).slots;
  }

  /**
   * Get all the slots that can storage any item stack.
   * Excludes armor slots and crafting slots.
   * Includes inventory slots, hotbar slots, and offhand slot.
   * @param lp local player object
   * @return sublist of slots
   */
  public static List<Slot> getStorageSlots(ClientPlayerEntity lp) {
    return getSlots(lp).subList(9, 46);
  }

  /**
   * Get all the storage slots excluding the offhand slot
   * @param lp local player object
   * @return sublist of slots
   */
  public static List<Slot> getPrimarySlots(ClientPlayerEntity lp) {
    return getSlots(lp).subList(9, 45);
  }

  /**
   * Get the storage slots excluding the offhand slot and hotbar slots
   * @param lp local player object
   * @return sublist of slots
   */
  public static List<Slot> getTopSlots(ClientPlayerEntity lp) {
    return getSlots(lp).subList(9, 36);
  }

  /**
   * Get all the hotbar storage slots
   * @param lp local player object
   * @return sublist of slots
   */
  public static List<Slot> getHotbarSlots(ClientPlayerEntity lp) {
    return getSlots(lp).subList(36, 45);
  }

  /**
   * Get the hotbar slots and offhand slot
   * @param lp local player object
   * @return sublist of slots
   */
  public static List<Slot> getHotbarAndOffhandSlots(ClientPlayerEntity lp) {
    return getSlots(lp).subList(36, 46);
  }

  public static ItemStack getMouseHeldItem(ClientPlayerEntity lp) {
    return getInventory(lp).getCarried();
  }

  public static Slot getMouseHeldSlot(ClientPlayerEntity lp) {
    return new HeldSlot(getInventory(lp));
  }

  public static int getSelectedIndex(ClientPlayerEntity lp) {
    return getInventory(lp).selected;
  }

  public static ItemStack getSelectedItem(ClientPlayerEntity lp) {
    return getInventory(lp).getSelected();
  }

  public static Slot getSelectedSlot(ClientPlayerEntity lp) {
    return getHotbarSlots(lp).get(getSelectedIndex(lp));
  }

  public static Runnable setSelectedIndex(ClientPlayerEntity lp, int index, boolean reset, Predicate<Long> condition) {
    if (index < 0 || index > getHotbarSize(lp) - 1) {
      throw new IllegalArgumentException("index must be between 0 and " + (getHotbarSize(lp) - 1) + ", got " + index);
    }

    SelectedItemData data = getSelectedItemData();
    PlayerInventory inv = getInventory(lp);
    int currentIndex = inv.selected;

    if (!reset) {
      inv.selected = index;

      if (data.originalIndex != -1) {
        data.originalIndex = index;
      }

      return () -> {
        inv.selected = currentIndex;

        if (data.originalIndex != -1) {
          data.originalIndex = currentIndex;
        }
      };
    } else {
      if (currentIndex != index) {
        if (data.originalIndex == -1) {
          data.originalIndex = currentIndex;
        }

        data.lastSetIndex = index;
        data.resetCondition = MoreObjects.firstNonNull(condition, ticks -> true);

        inv.selected = index;
      }

      data.ticks = 0;

      return () -> {
        if (index == inv.selected && data.lastSetIndex == index) {
          inv.selected = currentIndex;
          data.reset();
        }
      };
    }
  }

  public static Runnable setSelectedSlot(ClientPlayerEntity lp,
      Slot slot, boolean reset, Predicate<Long> condition) {
    return setSelectedIndex(lp, slot.getSlotIndex(), reset, condition);
  }

  public static Runnable setSelectedSlot(ClientPlayerEntity lp, Slot slot, Predicate<Long> condition) {
    return setSelectedIndex(lp, ItemEx.getHotbarIndex(slot), true, condition);
  }

  public static Runnable forceSelectedSlot(ClientPlayerEntity lp, Slot slot) {
    return setSelectedIndex(lp, ItemEx.getHotbarIndex(slot), false, null);
  }

  public static ItemStack getOffhandItem(ClientPlayerEntity lp) {
    return lp.getOffhandItem();
  }

  public static Slot getOffhandSlot(ClientPlayerEntity lp) {
    return getContainer(lp).getSlot(45);
  }

  public static ItemStack sendWindowClick(ClientPlayerEntity lp,
      Container openedContainer, PlayerInventory playerInventory,
      int slotIndex, int mouseButton, ClickType clickType) {
    short id = openedContainer.backup(playerInventory);
    ItemStack stack = openedContainer.clicked(slotIndex, mouseButton, clickType, lp);
    GeneralEx.dispatchNetworkPacket(getNetworkManager(), new CClickWindowPacket(0, slotIndex,
        mouseButton, clickType, stack, id));
    return stack;
  }

  public static ItemStack sendWindowClick(ClientPlayerEntity lp, int slotIndex, int mouseButton, ClickType clickType) {
    return sendWindowClick(lp, getOpenContainer(lp), getInventory(lp), slotIndex, mouseButton, clickType);
  }

  public static ItemStack throwHeldItem(ClientPlayerEntity lp) {
    return sendWindowClick(lp, getOpenContainer(lp), getInventory(lp), -999, 0, ClickType.THROW);
  }

  public static boolean isActivelyEating(ClientPlayerEntity lp) {
    return lp.isHandsBusy() && lp.getItemInHand(lp.getUsedItemHand()).getItem().isEdible();
  }

  public static boolean canPlaceBlock(ClientPlayerEntity lp, Block block, BlockPos pos) {
    return BlockEx.isPlaceable(block, getWorld(lp), pos);
  }

  public static boolean canPlaceBlock(ClientPlayerEntity lp, Block block) {
    return canPlaceBlock(lp, block, BlockPos.ZERO);
  }

  public static boolean canPlaceBlocksAt(ClientPlayerEntity lp, BlockPos pos) {
    val world = getWorld(lp);
    return world.getBlockState(pos).getShape(world, pos).isEmpty();
  }

  public static double getDiggingSpeedAt(ClientPlayerEntity lp, ItemStack stack, BlockPos pos) {
    return ItemEx.getDiggingSpeed(stack, lp, getWorld(lp).getBlockState(pos), pos);
  }

  private static final Switch FLY_SWITCH = new Switch("PlayerFlying") {
    @Override
    protected void onEnabled() {
      addScheduledTask(() -> {
        if (getLocalPlayer() == null || getLocalPlayer().abilities == null) {
          return;
        }

        getLocalPlayer().abilities.mayfly = true;
        getLocalPlayer().abilities.flying = true;
      });
    }

    @Override
    protected void onDisabled() {
      addScheduledTask(() -> {
        if (getLocalPlayer() == null || getLocalPlayer().abilities == null) {
          return;
        }

        PlayerAbilities gmCaps = new PlayerAbilities();
        getPlayerController().getPlayerMode().updatePlayerAbilities(gmCaps);

        PlayerAbilities capabilities = getLocalPlayer().abilities;
        capabilities.mayfly = gmCaps.mayfly;
        capabilities.flying = gmCaps.mayfly && capabilities.flying;
        capabilities.setFlyingSpeed(gmCaps.getFlyingSpeed());
      });
    }
  };

  public static Switch getFlySwitch() {
    return FLY_SWITCH;
  }

  @Getter
  public static class SelectedItemData {
    int originalIndex = -1;
    int lastSetIndex = -1;
    Predicate<Long> resetCondition = ticks -> true;
    long ticks = -1;

    public void tick() {
      if (ticks != -1) {
        ++ticks;
      }
    }

    public void reset() {
      originalIndex = -1;
      lastSetIndex = -1;
      resetCondition = ticks -> true;
      ticks = -1;
    }

    public void resetSelected(PlayerInventory inv) {
      if (originalIndex != -1 && inv.selected == lastSetIndex) {
        inv.selected = originalIndex;
      }
      reset();
    }

    public boolean testReset() {
      return resetCondition.test(ticks);
    }
  }
}
