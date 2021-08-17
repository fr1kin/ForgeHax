package dev.fiki.forgehax.main.mods.player;

import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.entity.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.api.extension.ItemEx;
import dev.fiki.forgehax.api.extension.LocalPlayerEx;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import lombok.experimental.ExtensionMethod;
import lombok.val;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import java.util.Comparator;

import static dev.fiki.forgehax.main.Common.getLocalPlayer;

@RegisterMod(
    name = "AutoMend",
    description = "Automatically swap item in offhand with another valid item once its fully repaired",
    category = Category.PLAYER
)
@ExtensionMethod({ItemEx.class, LocalPlayerEx.class})
public class AutoMend extends ToggleMod {
  private boolean isMendableTool(ItemStack stack) {
    return stack.canBeDamaged()
        && stack.hasEnchantment(Enchantments.MENDING);
  }

  private boolean isMendableTool(Slot slot) {
    return isMendableTool(slot.getItem());
  }

  @SubscribeListener
  public void onUpdate(LocalPlayerUpdateEvent event) {
    val lp = getLocalPlayer();

    if (!(lp.getOpenContainer() instanceof PlayerContainer)) {
      return;
    }

    final Slot currentSlot = lp.getSelectedSlot();
    final Slot offhandSlot = lp.getOffhandSlot();

    // check that we are holding a tool that is no longer damaged and has mending
    if (offhandSlot.hasItem()
        && !offhandSlot.getItem().isDamaged()
        && isMendableTool(offhandSlot)) {
      if (isMendableTool(offhandSlot)) {
        lp.getPrimarySlots().stream()
            .filter(this::isMendableTool)
            // replacement item should be damaged
            .filter(slot -> slot.getItem().isDamaged())
            // should not be the same slot as our main hand
            .filter(slot -> !currentSlot.isEqual(slot))
            // find the worst damaged item
            .min(Comparator.comparing(Slot::getItem, Comparator.comparing(ItemEx::getDurability)))
            .ifPresent(slot -> {
              // pickup replacement item
              slot.click(ClickType.PICKUP, 0);
              // place into offhand
              offhandSlot.click(ClickType.PICKUP, 0);
              // place what is in the offhand back into the inventory
              slot.click(ClickType.PICKUP, 0);
            });
      }
    }
  }
}
