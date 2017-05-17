package com.matt.forgehax.mods;

import com.google.common.collect.Lists;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.key.Bindings;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.FoodStats;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

import static com.matt.forgehax.Wrapper.*;

@RegisterMod
public class AutoEatMod extends ToggleMod {
    private boolean isEating = false;

    public AutoEatMod() {
        super("AutoEat", false, "Auto eats when you get hungry");
    }

    @SubscribeEvent
    public void onUpdate(LocalPlayerUpdateEvent event) {
        FoodStats foodStats = getLocalPlayer().getFoodStats();
        int foodSlot = -1;
        ItemStack foodStack = null;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = getLocalPlayer().inventory.getStackInSlot(i);
            if (stack != null &&
                    stack.getItem() instanceof ItemFood) {
                foodSlot = i;
                foodStack = stack;
                break;
            }
        }
        if (foodStack != null) {
            ItemFood itemFood = (ItemFood) foodStack.getItem();
            if (20 - foodStats.getFoodLevel() >= itemFood.getHealAmount(foodStack)) {
                isEating = true;
                MC.player.inventory.currentItem = foodSlot;
                Bindings.use.setPressed(true);
                return;
            }
        }
        if(isEating) {
            Bindings.use.setPressed(false);
            isEating = false;
        }
    }
}
