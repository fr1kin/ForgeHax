package com.matt.forgehax.mods;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import java.util.OptionalInt;
import java.util.stream.IntStream;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class AutoTotemMod extends ToggleMod {
  
  private final int OFFHAND_SLOT = 45;
  
  public AutoTotemMod() {
    super(Category.COMBAT, "AutoTotem", false, "Automatically move totems to off-hand");
  }
  
  @Override
  public String getDisplayText() {
    final long totemCount =
        IntStream.rangeClosed(9, 45) // include offhand slot
            .mapToObj(i -> MC.player.inventoryContainer.getSlot(i).getStack().getItem())
            .filter(stack -> stack == Items.TOTEM_OF_UNDYING)
            .count();
    return String.format(super.getDisplayText() + "[%d]", totemCount);
  }
  
  @SubscribeEvent
  public void onPlayerUpdate(LocalPlayerUpdateEvent event) {
    if (!getOffhand().isEmpty()) {
      return; // if there's an item in offhand slot
    }
    if (MC.currentScreen != null) {
      return; // if in inventory
    }
    
    findItem(Items.TOTEM_OF_UNDYING)
        .ifPresent(
            slot -> {
              invPickup(slot);
              invPickup(OFFHAND_SLOT);
            });
  }
  
  private void invPickup(final int slot) {
    MC.playerController.windowClick(0, slot, 0, ClickType.PICKUP, MC.player);
  }
  
  private OptionalInt findItem(final Item ofType) {
    for (int i = 9; i <= 44; i++) {
      if (MC.player.inventoryContainer.getSlot(i).getStack().getItem() == ofType) {
        return OptionalInt.of(i);
      }
    }
    return OptionalInt.empty();
  }
  
  private ItemStack getOffhand() {
    return MC.player.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND);
  }
}
