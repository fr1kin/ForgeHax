package dev.fiki.forgehax.main.mods.combat;

import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.main.util.entity.LocalPlayerInventory;
import dev.fiki.forgehax.main.util.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.OptionalInt;
import java.util.stream.IntStream;

@RegisterMod(
    name = "AutoTotem",
    description = "Automatically move totems to off-hand",
    category = Category.COMBAT
)
public class AutoTotemMod extends ToggleMod {

  private final int OFFHAND_SLOT = 45;

  private final BooleanSetting allowGui = newBooleanSetting()
      .name("allow-gui")
      .description("Lets AutoTotem work in menus.")
      .defaultTo(false)
      .build();

  @Override
  public String getDisplayText() {
    final long totemCount = IntStream.rangeClosed(9, 45) // include offhand slot
        .mapToObj(i -> LocalPlayerInventory.getContainer().getSlot(i).getStack().getItem())
        .filter(Items.TOTEM_OF_UNDYING::equals)
        .count();
    return String.format(super.getDisplayText() + "[%d]", totemCount);
  }

  @SubscribeEvent
  public void onPlayerUpdate(LocalPlayerUpdateEvent event) {
    if (!getOffhand().isEmpty() // if there's an item in offhand slot
        // if in inventory
        || (Common.getDisplayScreen() != null && !allowGui.getValue())) {
      return; // if there's an item in offhand slot
    }

    findItem(Items.TOTEM_OF_UNDYING).ifPresent(slot -> {
      invPickup(slot);
      invPickup(OFFHAND_SLOT);
    });
  }

  private void invPickup(final int slot) {
    Common.getPlayerController().windowClick(0, slot, 0, ClickType.PICKUP, Common.getLocalPlayer());
  }

  private OptionalInt findItem(final Item item) {
    for (int i = 9; i <= 44; i++) {
      if (LocalPlayerInventory.getContainer().getSlot(i).getStack().getItem() == item) {
        return OptionalInt.of(i);
      }
    }
    return OptionalInt.empty();
  }

  private ItemStack getOffhand() {
    return Common.getLocalPlayer().getItemStackFromSlot(EquipmentSlotType.OFFHAND);
  }
}
