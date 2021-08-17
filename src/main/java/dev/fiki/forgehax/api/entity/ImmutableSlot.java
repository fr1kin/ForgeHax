package dev.fiki.forgehax.api.entity;

import dev.fiki.forgehax.api.extension.ItemEx;
import lombok.Getter;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class ImmutableSlot extends Slot {
  @Getter
  private final ItemStack stack;

  public ImmutableSlot(Slot slot) {
    super(slot.container, slot.getSlotIndex(), slot.x, slot.y);
    this.index = ItemEx.getSlotNumber(slot);
    this.stack = slot.getItem().copy();
  }

  public ItemStack getStackFromInventory() {
    return super.getItem();
  }
}
