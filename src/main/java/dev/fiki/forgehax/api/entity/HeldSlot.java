package dev.fiki.forgehax.api.entity;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class HeldSlot extends Slot {
  public HeldSlot(PlayerInventory inventoryIn) {
    super(inventoryIn, -999, -1, -1);
  }

  @Override
  public ItemStack getItem() {
    return ((PlayerInventory) this.container).getCarried();
  }
}
