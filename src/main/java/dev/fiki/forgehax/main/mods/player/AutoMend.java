package dev.fiki.forgehax.main.mods.player;

import dev.fiki.forgehax.api.entity.LocalPlayerInventory;
import dev.fiki.forgehax.api.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Comparator;
import java.util.Optional;

@RegisterMod(
    name = "AutoMend",
    description = "Automatically swap item in offhand with another valid item once its fully repaired",
    category = Category.PLAYER
)
public class AutoMend extends ToggleMod {
  private boolean isMendable(LocalPlayerInventory.InvItem item) {
    return item.isItemDamageable()
        && EnchantmentHelper.getEnchantmentLevel(Enchantments.MENDING, item.getItemStack()) > 0;
  }

  private boolean isDamaged(LocalPlayerInventory.InvItem item) {
    return item.getItemStack().isDamaged();
  }

  @SubscribeEvent
  public void onUpdate(LocalPlayerUpdateEvent event) {
    if (!(LocalPlayerInventory.getOpenContainer() instanceof PlayerContainer)) {
      return;
    }

    LocalPlayerInventory.InvItem current = LocalPlayerInventory.getSelected();

    Optional.of(LocalPlayerInventory.getOffhand())
        .filter(this::isMendable)
        .filter(item -> !isDamaged(item))
        .ifPresent(offhand -> LocalPlayerInventory.getSlotInventory()
            .stream()
            .filter(this::isMendable)
            .filter(this::isDamaged)
            .filter(inv -> inv.getIndex() != current.getIndex())
            .max(Comparator.comparingInt(LocalPlayerInventory.InvItem::getDamage))
            .ifPresent(inv -> {
              // pick up
              LocalPlayerInventory.sendWindowClick(inv, 0, ClickType.PICKUP);
              // place in offhand
              LocalPlayerInventory.sendWindowClick(offhand, 0, ClickType.PICKUP);
              // place shovel back
              LocalPlayerInventory.sendWindowClick(inv, 0, ClickType.PICKUP);
            }));
  }
}
