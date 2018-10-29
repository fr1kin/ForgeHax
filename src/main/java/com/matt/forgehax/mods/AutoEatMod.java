package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getPlayerController;
import static com.matt.forgehax.Helper.getWorld;

import com.matt.forgehax.asm.events.ItemStoppedUsedEvent;
import com.matt.forgehax.asm.events.LeftClickCounterUpdateEvent;
import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.asm.reflection.FastReflection.Fields;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.mods.ESP.DrawOptions;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.entity.LocalPlayerInventory;
import com.matt.forgehax.util.entity.LocalPlayerInventory.InvItem;
import com.matt.forgehax.util.key.Bindings;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import java.util.Comparator;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.FoodStats;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class AutoEatMod extends ToggleMod {
  enum Sorting {
    POINTS,
    SATURATION,
    RATIO,
    ;
  }

  public final Setting<Sorting> sorting =
      getCommandStub()
          .builders()
          .<Sorting>newSettingEnumBuilder()
          .name("sorting")
          .description("Method used to find best food item to use")
          .defaultTo(Sorting.RATIO)
          .build();

  private boolean isEating = false;

  public AutoEatMod() {
    super(Category.PLAYER, "AutoEat", false, "Auto eats when you get hungry");
  }

  private boolean isFoodItem(InvItem inv) {
    return inv.getItem() instanceof ItemFood;
  }

  private double getHealAmount(InvItem inv) {
    return ((ItemFood)inv.getItem()).getHealAmount(inv.getItemStack());
  }

  private double getSaturationAmount(InvItem inv) {
    return ((ItemFood)inv.getItem()).getSaturationModifier(inv.getItemStack());
  }

  private double getPreferenceValue(InvItem inv) {
    switch (sorting.get()) {
      case POINTS:
        return getHealAmount(inv);
      case SATURATION:
        return getSaturationAmount(inv);
      case RATIO:
      default:
          return  getHealAmount(inv) / getSaturationAmount(inv);
    }
  }

  private boolean shouldEat(InvItem inv) {
    return 20 - getLocalPlayer().getFoodStats().getFoodLevel() >= getHealAmount(inv);
  }

  @SubscribeEvent
  public void onUpdate(LocalPlayerUpdateEvent event) {
    isEating = false;

    LocalPlayerInventory.getHotbarInventory().stream()
        .filter(InvItem::nonEmpty)
        .filter(this::isFoodItem)
        .max(Comparator.comparingDouble(this::getPreferenceValue).thenComparing(LocalPlayerInventory::getHotbarDistance))
        .filter(this::shouldEat)
        .ifPresent(best -> {
          LocalPlayerInventory.setSelected(best,
              ticks -> !getLocalPlayer().isHandActive()
                  || !LocalPlayerInventory.getSelected().equals(best));

          Fields.Minecraft_rightClickDelayTimer.set(MC,  4);
          getPlayerController().processRightClick(getLocalPlayer(), getWorld(), EnumHand.MAIN_HAND);
          isEating = true;
        });
  }

  @SubscribeEvent
  public void onStopUse(ItemStoppedUsedEvent event) {
    if(isEating) event.setCanceled(true);
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void onGuiOpened(GuiOpenEvent event) {
    // process keys and mouse input even if this gui is open
    if (isEating && getWorld() != null && getLocalPlayer() != null && event.getGui() != null)
      event.getGui().allowUserInput = true;
  }

  @SubscribeEvent
  public void onLeftClickCouterUpdate(LeftClickCounterUpdateEvent event) {
    if(isEating) {
      // prevent the leftClickCounter from changing
      event.setCanceled(true);
    }
  }
}
