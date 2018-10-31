package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getPlayerController;
import static com.matt.forgehax.Helper.getWorld;

import com.google.common.collect.Streams;
import com.matt.forgehax.asm.events.ItemStoppedUsedEvent;
import com.matt.forgehax.asm.reflection.FastReflection.Fields;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.entity.LocalPlayerInventory;
import com.matt.forgehax.util.entity.LocalPlayerInventory.InvItem;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.item.ItemFishFood;
import net.minecraft.item.ItemFishFood.FishType;
import net.minecraft.item.ItemFood;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class AutoEatMod extends ToggleMod {
  private static final List<Potion> BAD_POTIONS =
      Streams.stream(Potion.REGISTRY).filter(Potion::isBadEffect).collect(Collectors.toList());

  enum Sorting {
    POINTS,
    SATURATION,
    RATIO,
    ;
  }

  private final Setting<Sorting> sorting =
      getCommandStub()
          .builders()
          .<Sorting>newSettingEnumBuilder()
          .name("sorting")
          .description("Method used to find best food item to use")
          .defaultTo(Sorting.RATIO)
          .build();

  private final Setting<Integer> fail_safe_multiplier =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("fail-safe-multiplier")
          .description(
              "Will attempt to eat again after use ticks * multiplier has elapsed. Set to 0 to disable")
          .defaultTo(10)
          .min(0)
          .max(20)
          .build();

  private ItemFood food = null;
  private boolean eating = false;
  private int ticksElapsed = -1;

  public AutoEatMod() {
    super(Category.PLAYER, "AutoEat", false, "Auto eats when you get hungry");
  }

  private void reset() {
    food = null;
    eating = false;
    ticksElapsed = -1;
  }

  private boolean isFoodItem(InvItem inv) {
    return inv.getItem() instanceof ItemFood;
  }

  private boolean isFishFood(InvItem inv) {
    return inv.getItem() instanceof ItemFishFood;
  }

  private ItemFood toFood(InvItem inv) {
    return (ItemFood) inv.getItem();
  }

  private ItemFishFood toFishFood(InvItem inv) {
    return (ItemFishFood) inv.getItem();
  }

  private boolean isGoodFood(InvItem inv) {
    PotionEffect pe = Fields.ItemFood_potionId.get(inv.getItem());
    return pe == null || isFishFood(inv)
        ? !FishType.PUFFERFISH.equals(FishType.byItemStack(inv.getItemStack()))
        : BAD_POTIONS.stream().filter(Potion::isBadEffect).noneMatch(pe.getPotion()::equals);
  }

  private int getHealAmount(InvItem inv) {
    return toFood(inv).getHealAmount(inv.getItemStack());
  }

  private double getSaturationAmount(InvItem inv) {
    return toFood(inv).getSaturationModifier(inv.getItemStack());
  }

  private int getHealthLevel(InvItem inv) {
    return Math.min(getLocalPlayer().getFoodStats().getFoodLevel() + getHealAmount(inv), 20);
  }

  private double getSaturationLevel(InvItem inv) {
    return Math.min(
        getLocalPlayer().getFoodStats().getSaturationLevel()
            + getHealAmount(inv) * getSaturationAmount(inv) * 2.D,
        20.D);
  }

  private double getPreferenceValue(InvItem inv) {
    switch (sorting.get()) {
      case POINTS:
        return getHealAmount(inv);
      case SATURATION:
        return getSaturationAmount(inv);
      case RATIO:
      default:
        return (getHealAmount(inv) * getSaturationAmount(inv) * 2.D) / getHealAmount(inv);
    }
  }

  private boolean shouldEat(InvItem inv) {
    return getLocalPlayer().getFoodStats().getFoodLevel() + getHealAmount(inv) < 20;
  }

  @Override
  protected void onEnabled() {
    food = null;
    eating = false;
    ticksElapsed = -1;
  }

  @SubscribeEvent
  public void onUpdate(LocalPlayerUpdateEvent event) {
    if (getLocalPlayer().isCreative()) return;

    eating = false;

    LocalPlayerInventory.getHotbarInventory()
        .stream()
        .filter(InvItem::nonEmpty)
        .filter(this::isFoodItem)
        .filter(this::isGoodFood)
        .max(
            Comparator.comparingDouble(this::getPreferenceValue)
                .thenComparing(LocalPlayerInventory::getHotbarDistance))
        .filter(this::shouldEat)
        .ifPresent(
            best -> {
              food = (ItemFood) best.getItem();

              LocalPlayerInventory.setSelected(
                  best,
                  ticks ->
                      !getLocalPlayer().isHandActive()
                          || !LocalPlayerInventory.getSelected().equals(best));

              Fields.Minecraft_rightClickDelayTimer.set(MC, 4);
              getPlayerController()
                  .processRightClick(getLocalPlayer(), getWorld(), EnumHand.MAIN_HAND);

              eating = true;
              ++ticksElapsed;
            });

    if (!eating) reset();
  }

  @SubscribeEvent
  public void onStopUse(ItemStoppedUsedEvent event) {
    if (food != null && eating) {
      if (fail_safe_multiplier.get() == 0
          || ticksElapsed < food.itemUseDuration * fail_safe_multiplier.get())
        event.setCanceled(true);
      else reset();
    }
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void onGuiOpened(GuiOpenEvent event) {
    // process keys and mouse input even if this gui is open
    if (eating && getWorld() != null && getLocalPlayer() != null && event.getGui() != null)
      event.getGui().allowUserInput = true;
  }
}
