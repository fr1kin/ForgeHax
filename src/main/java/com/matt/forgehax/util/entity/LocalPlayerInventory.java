package com.matt.forgehax.util.entity;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getNetworkManager;
import static com.matt.forgehax.Helper.getPlayerController;
import static com.matt.forgehax.asm.reflection.FastReflection.Fields.PlayerControllerMP_currentPlayerItem;

import com.google.common.base.Predicates;
import com.matt.forgehax.mods.services.HotbarSelectionService;
import com.matt.forgehax.mods.services.HotbarSelectionService.ResetFunction;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;

public class LocalPlayerInventory {
  public static InventoryPlayer getInventory() {
    return getLocalPlayer().inventory;
  }

  public static Container getContainer() {
    return getLocalPlayer().inventoryContainer;
  }

  public static Container getOpenContainer() {
    return getLocalPlayer().openContainer;
  }

  public static int getHotbarSize() {
    return InventoryPlayer.getHotbarSize();
  }

  public static List<InvItem> getMainInventory() {
    AtomicInteger next = new AtomicInteger(0);
    return getInventory()
        .mainInventory
        .stream()
        .map(item -> new InvItem.Base(item, next.getAndIncrement()))
        .collect(Collectors.toList());
  }

  public static List<InvItem> getMutatingInventory() {
    return getContainer()
        .inventorySlots
        .stream()
        .map(InvItem.Mutating::new)
        .collect(Collectors.toList());
  }

  public static List<InvItem> getMainInventory(int start, int end) {
    return getMainInventory().subList(start, end);
  }

  public static List<InvItem> getMutatingInventory(int start, int end) {
    return getMutatingInventory().subList(start, end);
  }

  public static List<InvItem> getStorageInventory() {
    return getMainInventory(9, 27);
  }

  public static List<InvItem> getMutatingStorageInventory() {
    return getMutatingInventory(9, 36);
  }

  public static List<InvItem> getHotbarInventory() {
    return getMainInventory(0, getHotbarSize());
  }

  public static List<InvItem> getMutatingHotbarInventory() {
    return getMutatingInventory(27, 36);
  }

  public static InvItem getSelected() {
    return getMainInventory().get(getInventory().currentItem);
  }

  public static ResetFunction setSelected(int index, boolean reset, Predicate<Long> condition) {
    return HotbarSelectionService.getInstance().setSelected(index, reset, condition);
  }

  public static ResetFunction setSelected(InvItem inv, boolean reset, Predicate<Long> condition) {
    return setSelected(inv.getIndex(), reset, condition);
  }

  public static ResetFunction setSelected(int index, Predicate<Long> condition) {
    Objects.requireNonNull(condition);
    return setSelected(index, true, condition);
  }

  public static ResetFunction setSelected(InvItem inv, Predicate<Long> condition) {
    return setSelected(inv.getIndex(), condition);
  }

  public static ResetFunction setSelected(int index) {
    return setSelected(index, Predicates.alwaysTrue());
  }

  public static ResetFunction setSelected(InvItem invItem) {
    return setSelected(invItem.getIndex());
  }

  public static ResetFunction forceSelected(int index) {
    return setSelected(index, false, null);
  }

  public static ResetFunction forceSelected(InvItem inv) {
    return forceSelected(inv.getIndex());
  }

  public static void resetSelected() {
    HotbarSelectionService.getInstance().resetSelected();
  }

  public static void syncSelected() {
    int selected = getSelected().getIndex();
    if (selected != PlayerControllerMP_currentPlayerItem.get(getPlayerController())) {
      PlayerControllerMP_currentPlayerItem.set(getPlayerController(), selected);
      getNetworkManager().sendPacket(new CPacketHeldItemChange(selected));
    }
  }

  public static int getHotbarDistance(InvItem item) {
    int max = LocalPlayerInventory.getHotbarSize() - 1;
    return item.getIndex() > max ? 0 : max - Math.abs(getSelected().getIndex() - item.getIndex());
  }

  public static InvItem newInvItem(ItemStack itemStack, int index) {
    return new InvItem.Base(itemStack, index);
  }

  public static InvItem newInvItem(Slot slot) {
    return new InvItem.Mutating(slot);
  }

  public abstract static class InvItem implements Comparable<InvItem> {
    public static final InvItem EMPTY =
        new InvItem() {
          @Override
          public ItemStack getItemStack() {
            return ItemStack.EMPTY;
          }

          @Override
          public Item getItem() {
            return Items.AIR;
          }

          @Override
          public int getIndex() {
            return -1;
          }
        };

    public abstract ItemStack getItemStack();

    public abstract Item getItem();

    public abstract int getIndex();

    public boolean isNull() {
      return ItemStack.EMPTY.equals(getItemStack());
    }

    public boolean nonNull() {
      return !isNull();
    }

    @Override
    public boolean equals(Object obj) {
      return this == obj
          || (obj instanceof InvItem
              && getIndex() == ((InvItem) obj).getIndex()
              && getItemStack().equals(((InvItem) obj).getItemStack()));
    }

    @Override
    public int hashCode() {
      return Objects.hash(getItemStack(), getIndex());
    }

    @Override
    public int compareTo(InvItem o) {
      return Integer.compare(getIndex(), o.getIndex());
    }

    private static class Base extends InvItem {
      private final ItemStack itemStack;
      private final int index;

      public Base(ItemStack itemStack, int index) {
        this.itemStack = itemStack;
        this.index = index;
      }

      @Override
      public ItemStack getItemStack() {
        return itemStack;
      }

      @Override
      public Item getItem() {
        return itemStack.getItem();
      }

      @Override
      public int getIndex() {
        return index;
      }
    }

    private static class Mutating extends InvItem {
      private final Slot slot;

      public Mutating(Slot slot) {
        this.slot = slot;
      }

      @Override
      public ItemStack getItemStack() {
        return slot.getStack();
      }

      @Override
      public Item getItem() {
        return slot.getStack().getItem();
      }

      @Override
      public int getIndex() {
        return slot.slotNumber;
      }
    }
  }
}
