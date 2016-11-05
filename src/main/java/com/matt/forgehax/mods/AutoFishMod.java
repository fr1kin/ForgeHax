package com.matt.forgehax.mods;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created on 9/2/2016 by fr1kin
 */
public class AutoFishMod extends ToggleMod {
    private final static int CAST_DELAY = 20;

    private int castTickDelay = 0;

    private boolean lineCasted = false;
    private int tickToRecast = 0;

    public AutoFishMod(String modName, boolean defaultValue, String description, int key) {
        super(modName, defaultValue, description, key);
    }

    @Override
    public void onEnabled() {
        castTickDelay = 0;
        lineCasted = false;
        tickToRecast = 0;
    }

    @SubscribeEvent
    public void onUpdate(LocalPlayerUpdateEvent event) {
        EntityPlayer localPlayer = getLocalPlayer();
        ItemStack heldStack = localPlayer.getHeldItemMainhand();
        // cast timer
        if(castTickDelay > 0) castTickDelay--;
        // recast timer
        if(tickToRecast > 0) tickToRecast--;

        // check if player is holding a fishing rod
        if(heldStack != null &&
                heldStack.getItem() instanceof ItemFishingRod) {
            if(localPlayer.fishEntity != null) {
                // player is currently fishing
                EntityFishHook fishHook = localPlayer.fishEntity;
                Block blockOn = getWorld().getBlockState(new BlockPos(fishHook.posX, fishHook.posY - 1, fishHook.posZ)).getBlock();
                if(lineCasted &&
                        !blockOn.equals(Blocks.FLOWING_WATER) &&
                        fishHook.motionX == 0.D &&
                        fishHook.motionY != 0.D &&
                        fishHook.motionZ == 0.D) {
                    // pull fish in
                    MC.rightClickMouse();
                    lineCasted = false;
                    castTickDelay = CAST_DELAY;
                }
            } else {
                // hook is not deployed
                if((!lineCasted ||
                        tickToRecast <= 0) &&
                        castTickDelay <= 0) {
                    // line is not casted
                    MC.rightClickMouse();
                    lineCasted = true;
                    tickToRecast = CAST_DELAY;
                    castTickDelay = 0;
                }
            }
        }
    }
}
