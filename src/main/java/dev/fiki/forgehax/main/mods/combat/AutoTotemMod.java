package dev.fiki.forgehax.main.mods.combat;

import dev.fiki.forgehax.api.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.entity.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.api.extension.ItemEx;
import dev.fiki.forgehax.api.extension.LocalPlayerEx;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import lombok.experimental.ExtensionMethod;
import lombok.val;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import static dev.fiki.forgehax.main.Common.getDisplayScreen;
import static dev.fiki.forgehax.main.Common.getLocalPlayer;

@RegisterMod(
    name = "AutoTotem",
    description = "Automatically move totems to off-hand",
    category = Category.COMBAT
)
@ExtensionMethod({ItemEx.class, LocalPlayerEx.class})
public class AutoTotemMod extends ToggleMod {
  private final BooleanSetting allowGui = newBooleanSetting()
      .name("allow-gui")
      .description("Lets AutoTotem work in menus.")
      .defaultTo(false)
      .build();

  @Override
  public String getDisplayText() {
    final long totemCount = getLocalPlayer().getSlots().stream() // include offhand slot
        .map(Slot::getItem)
        .map(ItemStack::getItem)
        .filter(Items.TOTEM_OF_UNDYING::equals)
        .count();
    return String.format(super.getDisplayText() + "[%d]", totemCount);
  }

  @SubscribeListener
  public void onPlayerUpdate(LocalPlayerUpdateEvent event) {
    val lp = getLocalPlayer();
    if (!lp.getOffhandSlot().hasItem()
        && (allowGui.isDisabled() || getDisplayScreen() == null)) {
      lp.getPrimarySlots().stream()
          .filter(slot -> Items.TOTEM_OF_UNDYING.equals(slot.getItem().getItem()))
          .findAny()
          .ifPresent(slot -> lp.getOffhandSlot().swap(slot));
    }
  }
}
