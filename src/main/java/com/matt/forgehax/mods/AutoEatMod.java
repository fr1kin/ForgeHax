package com.matt.forgehax.mods;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Streams;
import com.matt.forgehax.Globals;
import com.matt.forgehax.asm.events.ItemStoppedUsedEvent;
import com.matt.forgehax.asm.reflection.FastReflection.Fields;
import com.matt.forgehax.events.ForgeHaxEvent;
import com.matt.forgehax.events.ForgeHaxEvent.Type;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.entity.LocalPlayerInventory;
import com.matt.forgehax.util.entity.LocalPlayerInventory.InvItem;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import net.minecraft.item.Food;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.util.Hand;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import static com.matt.forgehax.Globals.*;

@RegisterMod
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
  
  private final Setting<Integer> select_wait =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("select-wait")
          .description(
              "Number of ticks to wait before starting to eat a food item after switching to it in the hotbar.")
          .defaultTo(10)
          .min(0)
          .build();
  
  private Food food = null;
  private boolean eating = false;
  private int eatingTicks = 0;
  private int selectedTicks = 0;
  private int lastHotbarIndex = -1;
  
  public AutoEatMod() {
    super(Category.PLAYER, "AutoEat", false, "Auto eats when you get hungry");
  }
  
  private void reset() {
    if (eatingTicks > 0) {
      addScheduledTask(() -> MinecraftForge.EVENT_BUS.post(new ForgeHaxEvent(Type.EATING_STOP)));
    }
    food = null;
    eating = false;
    eatingTicks = 0;
    selectedTicks = 0;
  }
  
  private boolean isFoodItem(InvItem inv) {
    return ItemGroup.FOOD.equals(inv.getItem().getGroup());
  }
  
  private boolean isFishFood(InvItem inv) {
    return ItemGroup.FOOD.equals(inv.getItem().getGroup());
  }
  
  private Food toFood(InvItem inv) {
    return inv.getItem().getFood();
  }
  
  private boolean isGoodFood(InvItem inv) {
    return inv.getItem().getFood().getEffects().stream()
        .map(Pair::getLeft)
        .map(EffectInstance::getPotion)
        .anyMatch(BAD_POTIONS::contains);
  }
  
  private int getHealAmount(InvItem inv) {
    return toFood(inv).getHealing();
  }
  
  private double getSaturationAmount(InvItem inv) {
    return toFood(inv).getSaturation();
  }
  
  private int getHealthLevel(InvItem inv) {
    return Math.min(getLocalPlayer().getFoodStats().getFoodLevel() + getHealAmount(inv), 20);
  }
  
  private double getSaturationLevel(InvItem inv) {
    return Math.min(getLocalPlayer().getFoodStats().getSaturationLevel()
            + getHealAmount(inv) * getSaturationAmount(inv) * 2.D, 20.D);
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
  
  private boolean checkFailsafe() { // TODO: replace 500 with longest food duration
    return (fail_safe_multiplier.get() == 0 || eatingTicks < 500 * fail_safe_multiplier.get());
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
        .filter(InvItem::nonEmpty)
        .filter(this::isFoodItem)
        .filter(this::isGoodFood)
        .max(
            Comparator.comparingDouble(this::getPreferenceValue)
                .thenComparing(LocalPlayerInventory::getHotbarDistance))
        .filter(this::shouldEat)
        .ifPresent(
            best -> {
              food = best.getItem().getFood();
              
              LocalPlayerInventory.setSelected(best, ticks -> !eating);
              
              eating = true;
              
              if (!checkFailsafe()) {
                reset();
                eating = true;
                return;
              }
              
              if (currentSelected != best.getIndex()) {
                MinecraftForge.EVENT_BUS.post(new ForgeHaxEvent(Type.EATING_SELECT_FOOD));
                lastHotbarIndex = best.getIndex();
                selectedTicks = 0;
              }
              
              if (selectedTicks > select_wait.get()) {
                if (!wasEating) {
                  MinecraftForge.EVENT_BUS.post(new ForgeHaxEvent(Type.EATING_START));
                }
                
                Fields.Minecraft_rightClickDelayTimer.set(MC, 4);
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
  
//  @SubscribeEvent(priority = EventPriority.HIGHEST)
//  public void onGuiOpened(GuiOpenEvent event) {
//    // process keys and mouse input even if this gui is open
//    if (eating && getWorld() != null && getLocalPlayer() != null && event.getGui() != null) {
//      event.getGui().allowUserInput = true;
//    }
//  } // TODO: 1.15 might need to find alternative
}
