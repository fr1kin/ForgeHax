package dev.fiki.forgehax.main.mods.player;

import com.mojang.datafixers.util.Pair;
import dev.fiki.forgehax.api.cmd.settings.EnumSetting;
import dev.fiki.forgehax.api.cmd.settings.IntegerSetting;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.entity.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.api.extension.ItemEx;
import dev.fiki.forgehax.api.extension.LocalPlayerEx;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.asm.events.game.ItemStoppedUsedEvent;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import lombok.val;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.Hand;

import java.util.Comparator;

import static dev.fiki.forgehax.main.Common.getLocalPlayer;

@RegisterMod(
    name = "AutoEat",
    description = "Auto eats when you get hungry",
    category = Category.PLAYER
)
@RequiredArgsConstructor
@ExtensionMethod({ItemEx.class, LocalPlayerEx.class})
public class AutoEatMod extends ToggleMod {
  enum Sorting {
    POINTS,
    SATURATION,
    RATIO,
    ;
  }

  private final EnumSetting<Sorting> sorting = newEnumSetting(Sorting.class)
      .name("sorting")
      .description("Method used to find best food item to use")
      .defaultTo(Sorting.RATIO)
      .build();

  private final IntegerSetting failSafeMultiplier = newIntegerSetting()
      .name("fail-safe-multiplier")
      .description("Will attempt to eat again after use ticks * multiplier has elapsed. Set to 0 to disable")
      .defaultTo(10)
      .min(0)
      .max(20)
      .build();

  private final IntegerSetting selectWait = newIntegerSetting()
      .name("select-wait")
      .description("Number of ticks to wait before starting to eat a food item after switching to it in the hotbar.")
      .defaultTo(10)
      .min(0)
      .build();

  private Slot targetSlot = null;
  private int eatingTicks = 0;
  private int selectedTicks = 0;
  private Runnable resetSelected = null;

  private void reset() {
    targetSlot = null;
    eatingTicks = 0;
    selectedTicks = 0;
    if (resetSelected != null) {
      resetSelected.run();
      resetSelected = null;
    }
  }

  private boolean isGoodFood(Slot slot) {
    val stack = slot.getStack();
    return stack.getItem().isFood() &&
        stack.getItem().getFood().getEffects().stream()
            .map(Pair::getFirst)
            .map(EffectInstance::getPotion)
            .allMatch(Effect::isBeneficial);
  }

  private float getPreferenceValue(Slot slot) {
    val stack = slot.getStack();
    val food = stack.getItem().getFood();
    switch (sorting.getValue()) {
      case POINTS:
        return food.getHealing();
      case SATURATION:
        return food.getSaturation();
      case RATIO:
      default:
        return (food.getHealing() * food.getSaturation() * 2.f) / (float) food.getHealing();
    }
  }

  private boolean shouldEat(ClientPlayerEntity lp, Slot slot) {
    return lp.getFoodStats().getFoodLevel()
        + slot.getStack().getItem().getFood().getHealing() < 20;
  }

  private int getLongestEatingTicks(ItemStack stack) {
    return stack.getItem().getFood().isFastEating() ? 0 : stack.getUseDuration();
  }

  private boolean isEatingTooLong() {
    return targetSlot != null
        && failSafeMultiplier.intValue() > 0
        && eatingTicks > getLongestEatingTicks(targetSlot.getStack()) * failSafeMultiplier.intValue();
  }

  private boolean shouldRevertSelected(ClientPlayerEntity lp) {
    return selectedTicks <= 0 && !lp.isActivelyEating();
  }

  @Override
  protected void onEnabled() {
    reset();
  }

  @SubscribeListener
  public void onUpdate(LocalPlayerUpdateEvent event) {
    final ClientPlayerEntity lp = getLocalPlayer();
    if (lp.isCreative()) {
      return;
    }

    boolean wasEating = lp.isActivelyEating();

    if (targetSlot == null) {
      Slot foodSlot = lp.getPrimarySlots().stream()
          .filter(Slot::getHasStack)
          .filter(this::isGoodFood)
          // prefer items in the hotbar
          .max(Comparator.comparing(ItemEx::isInHotbar)
              // get the food with the preferred healing properties
              .thenComparingDouble(this::getPreferenceValue)
              // select the item closest to the hotbar
              .thenComparing(ItemEx::getDistanceFromSelected, Comparator.reverseOrder()))
          .orElse(null);

      if (foodSlot != null && shouldEat(lp, foodSlot)) {
        reset();
        // if the selected item is in our inventory we gotta swap items
        if (!foodSlot.isInHotbar()) {
          final Slot availableSlot = lp.getHotbarSlots().stream()
              .min(Comparator.comparing(Slot::getHasStack)
                  .thenComparing(Slot::getStack, Comparator.comparing(ItemEx::isFoodItem, Comparator.reverseOrder())))
              .orElseThrow(() -> new Error("You should not see this error"));
          // set current selected item
          resetSelected = lp.forceSelectedSlot(availableSlot);
          // item in hotbar with food item
          foodSlot.click(ClickType.SWAP, availableSlot.getHotbarIndex());
          foodSlot = availableSlot;
        } else {
          resetSelected = lp.forceSelectedSlot(foodSlot);
        }
        targetSlot = foodSlot.toImmutable();
      }
    }

    if (targetSlot != null
        && shouldEat(lp, targetSlot)
        && !isEatingTooLong()) {
      if (lp.getSelectedSlot().isEqual(targetSlot)) {
        ++selectedTicks;
      } else if (eatingTicks > 0 || selectedTicks > selectWait.intValue() + 1) {
        // we need to reset and retry
        reset();
      }

      if (selectedTicks > selectWait.intValue()) {
        if (!wasEating) {
          getLog().debug("Started eating {}", targetSlot.getStack());
        }

        lp.rightClick(Hand.MAIN_HAND);
        ++eatingTicks;
      }
    } else {
      reset();
    }
  }

  @SubscribeListener
  public void onStopUse(ItemStoppedUsedEvent event) {
    if (targetSlot != null && eatingTicks > 0) {
      if (!isEatingTooLong()) {
        event.setCanceled(true);
      } else {
        reset();
      }
    }
  }
}
