package dev.fiki.forgehax.main.mods;

import com.mojang.datafixers.util.Pair;
import dev.fiki.forgehax.asm.events.ItemStoppedUsedEvent;
import dev.fiki.forgehax.main.events.ForgeHaxEvent;
import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.cmd.settings.EnumSetting;
import dev.fiki.forgehax.main.util.cmd.settings.IntegerSetting;
import dev.fiki.forgehax.main.util.entity.LocalPlayerInventory;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import dev.fiki.forgehax.main.util.reflection.ReflectionTools;
import lombok.RequiredArgsConstructor;
import net.minecraft.item.Food;
import net.minecraft.item.ItemGroup;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.Hand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static dev.fiki.forgehax.main.Common.*;

@RegisterMod(
    name = "AutoEat",
    description = "Auto eats when you get hungry",
    category = Category.PLAYER
)
@RequiredArgsConstructor
public class AutoEatMod extends ToggleMod {
  private static final List<Effect> BAD_POTIONS =
      StreamSupport.stream(ForgeRegistries.POTIONS.spliterator(), false)
          .filter(effect -> !effect.isBeneficial())
          .collect(Collectors.toList());

  enum Sorting {
    POINTS,
    SATURATION,
    RATIO,
    ;
  }

  private final ReflectionTools reflection;

  private final EnumSetting<Sorting> sorting = newEnumSetting(Sorting.class)
      .name("sorting")
      .description("Method used to find best food item to use")
      .defaultTo(Sorting.RATIO)
      .build();

  private final IntegerSetting fail_safe_multiplier = newIntegerSetting()
      .name("fail-safe-multiplier")
      .description(
          "Will attempt to eat again after use ticks * multiplier has elapsed. Set to 0 to disable")
      .defaultTo(10)
      .min(0)
      .max(20)
      .build();

  private final IntegerSetting select_wait = newIntegerSetting()
      .name("select-wait")
      .description("Number of ticks to wait before starting to eat a food item after switching to it in the hotbar.")
      .defaultTo(10)
      .min(0)
      .build();

  private Food food = null;
  private boolean eating = false;
  private int eatingTicks = 0;
  private int selectedTicks = 0;
  private int lastHotbarIndex = -1;

  private void reset() {
    if (eatingTicks > 0) {
      addScheduledTask(() -> MinecraftForge.EVENT_BUS.post(new ForgeHaxEvent(ForgeHaxEvent.Type.EATING_STOP)));
    }
    food = null;
    eating = false;
    eatingTicks = 0;
    selectedTicks = 0;
  }

  private boolean isFoodItem(LocalPlayerInventory.InvItem inv) {
    return ItemGroup.FOOD.equals(inv.getItem().getGroup());
  }

  private boolean isFishFood(LocalPlayerInventory.InvItem inv) {
    return ItemGroup.FOOD.equals(inv.getItem().getGroup());
  }

  private Food toFood(LocalPlayerInventory.InvItem inv) {
    return inv.getItem().getFood();
  }

  private boolean isGoodFood(LocalPlayerInventory.InvItem inv) {
    return inv.getItem().getFood().getEffects().stream()
        .map(Pair::getFirst)
        .map(EffectInstance::getPotion)
        .anyMatch(BAD_POTIONS::contains);
  }

  private int getHealAmount(LocalPlayerInventory.InvItem inv) {
    return toFood(inv).getHealing();
  }

  private double getSaturationAmount(LocalPlayerInventory.InvItem inv) {
    return toFood(inv).getSaturation();
  }

  private int getHealthLevel(LocalPlayerInventory.InvItem inv) {
    return Math.min(getLocalPlayer().getFoodStats().getFoodLevel() + getHealAmount(inv), 20);
  }

  private double getSaturationLevel(LocalPlayerInventory.InvItem inv) {
    return Math.min(getLocalPlayer().getFoodStats().getSaturationLevel()
        + getHealAmount(inv) * getSaturationAmount(inv) * 2.D, 20.D);
  }

  private double getPreferenceValue(LocalPlayerInventory.InvItem inv) {
    switch (sorting.getValue()) {
      case POINTS:
        return getHealAmount(inv);
      case SATURATION:
        return getSaturationAmount(inv);
      case RATIO:
      default:
        return (getHealAmount(inv) * getSaturationAmount(inv) * 2.D) / getHealAmount(inv);
    }
  }

  private boolean shouldEat(LocalPlayerInventory.InvItem inv) {
    return getLocalPlayer().getFoodStats().getFoodLevel() + getHealAmount(inv) < 20;
  }

  private int getLongestEatingTicks(Food food) {
    return food.getEffects().stream()
        .map(Pair::getFirst)
        .map(EffectInstance::getDuration)
        .max(Integer::compareTo)
        .orElse(20);
  }

  private boolean checkFailsafe() {
    return (fail_safe_multiplier.getValue() == 0
        || eatingTicks < getLongestEatingTicks(food) * fail_safe_multiplier.getValue());
  }

  @Override
  protected void onEnabled() {
    reset();
    selectedTicks = 0;
    lastHotbarIndex = -1;
  }

  @SubscribeEvent
  public void onUpdate(LocalPlayerUpdateEvent event) {
    if (getLocalPlayer().isCreative()) {
      return;
    }

    int currentSelected = LocalPlayerInventory.getSelected().getIndex();

    boolean wasEating = eating;
    eating = false;

    LocalPlayerInventory.getHotbarInventory()
        .stream()
        .filter(LocalPlayerInventory.InvItem::nonEmpty)
        .filter(this::isFoodItem)
        .filter(this::isGoodFood)
        .max(Comparator.comparingDouble(this::getPreferenceValue)
            .thenComparing(LocalPlayerInventory::getHotbarDistance))
        .filter(this::shouldEat)
        .ifPresent(best -> {
          food = best.getItem().getFood();

          LocalPlayerInventory.setSelected(best, ticks -> !eating);

          eating = true;

          if (!checkFailsafe()) {
            reset();
            eating = true;
            return;
          }

          if (currentSelected != best.getIndex()) {
            MinecraftForge.EVENT_BUS.post(new ForgeHaxEvent(ForgeHaxEvent.Type.EATING_SELECT_FOOD));
            lastHotbarIndex = best.getIndex();
            selectedTicks = 0;
          }

          if (selectedTicks > select_wait.getValue()) {
            if (!wasEating) {
              MinecraftForge.EVENT_BUS.post(new ForgeHaxEvent(ForgeHaxEvent.Type.EATING_START));
            }

            reflection.Minecraft_rightClickDelayTimer.set(MC, 4);
            getPlayerController().processRightClick(getLocalPlayer(), getWorld(), Hand.MAIN_HAND);

            ++eatingTicks;
          }
        });

    if (lastHotbarIndex != -1) {
      if (lastHotbarIndex == LocalPlayerInventory.getSelected().getIndex()) {
        selectedTicks++;
      } else {
        selectedTicks = 0;
      }
    }

    lastHotbarIndex = LocalPlayerInventory.getSelected().getIndex();

    if (wasEating && !eating) {
      reset();
    }
  }

  @SubscribeEvent
  public void onStopUse(ItemStoppedUsedEvent event) {
    if (food != null && eating && eatingTicks > 0) {
      if (checkFailsafe()) {
        event.setCanceled(true);
      } else {
        reset();
      }
    }
  }
}
