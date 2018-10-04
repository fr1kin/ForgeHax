package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;

import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.key.Bindings;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.FoodStats;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class AutoEatMod extends ToggleMod {
  private boolean isEating = false;

  public AutoEatMod() {
    super(Category.PLAYER, "AutoEat", false, "Auto eats when you get hungry");
  }

  @SubscribeEvent
  public void onUpdate(LocalPlayerUpdateEvent event) {
    FoodStats foodStats = getLocalPlayer().getFoodStats();
    int foodSlot = -1;
    ItemStack foodStack = null;
    for (int i = 0; i < 9; i++) {
      ItemStack stack = getLocalPlayer().inventory.getStackInSlot(i);
      if (stack != null && stack.getItem() instanceof ItemFood) {
        foodSlot = i;
        foodStack = stack;
        break;
      }
    }
    if (foodStack != null) {
      ItemFood itemFood = (ItemFood) foodStack.getItem();
      if (20 - foodStats.getFoodLevel() >= itemFood.getHealAmount(foodStack)
          && !getLocalPlayer().isHandActive()
          && FastReflection.Fields.Minecraft_rightClickDelayTimer.get(MC) == 0) {
        isEating = true;
        MC.player.inventory.currentItem = foodSlot;
        // need to fake use key to stop code that stops the eating
        Bindings.use.setPressed(true);
        FastReflection.Methods.Minecraft_rightClickMouse.invoke(MC);
        return;
      }
    }
    if (isEating && !getLocalPlayer().isHandActive()) {
      isEating = false;
      Bindings.use.setPressed(false);
    }
  }
}
