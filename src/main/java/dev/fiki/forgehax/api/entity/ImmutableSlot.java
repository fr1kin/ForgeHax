package dev.fiki.forgehax.api.entity;

import dev.fiki.forgehax.api.extension.ItemEx;
import lombok.Getter;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class ImmutableSlot extends Slot {
  @Getter
  private final ItemStack stack;

  public ImmutableSlot(Slot slot) {
    super(slot.inventory, slot.getSlotIndex(), slot.xPos, slot.yPos);
    this.slotNumber = ItemEx.getSlotNumber(slot);
    this.stack = slot.getStack().copy();
  }

  public ItemStack getStackFromInventory() {
    return super.getStack();
  }
}
