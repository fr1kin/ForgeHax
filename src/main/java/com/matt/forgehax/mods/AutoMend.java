package com.matt.forgehax.mods;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.entity.LocalPlayerInventory;
import com.matt.forgehax.util.entity.LocalPlayerInventory.InvItem;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import java.util.Comparator;
import java.util.Optional;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class AutoMend extends ToggleMod {
  public AutoMend() {
    super(
        Category.PLAYER,
        "AutoMend",
        false,
        "Automatically swap item in offhand with another valid item once its fully repaired");
  }

  private boolean isMendable(InvItem item) {
    return item.isItemDamageable()
        && EnchantmentHelper.getEnchantmentLevel(Enchantments.MENDING, item.getItemStack()) > 0;
  }

  private boolean isDamaged(InvItem item) {
    return item.getItemStack().isItemDamaged();
  }

  @SubscribeEvent
  public void onUpdate(LocalPlayerUpdateEvent event) {
    if (!(LocalPlayerInventory.getOpenContainer() instanceof ContainerPlayer)) return;

    Optional.of(LocalPlayerInventory.getOffhand())
        .filter(this::isMendable)
        .filter(item -> !isDamaged(item))
        .ifPresent(
            offhand ->
                LocalPlayerInventory.getSlotInventory()
                    .stream()
                    .filter(this::isMendable)
                    .filter(this::isDamaged)
                    .max(Comparator.comparingInt(InvItem::getDamage))
                    .ifPresent(
                        inv -> {
                          // pick up
                          LocalPlayerInventory.sendWindowClick(inv, 0, ClickType.PICKUP);
                          // place in offhand
                          LocalPlayerInventory.sendWindowClick(offhand, 0, ClickType.PICKUP);
                          // place shovel back
                          LocalPlayerInventory.sendWindowClick(inv, 0, ClickType.PICKUP);
                        }));
  }
}
