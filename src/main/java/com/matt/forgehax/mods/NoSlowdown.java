package com.matt.forgehax.mods;

import com.matt.forgehax.asm.events.DoBlockCollisionsEvent;
import net.minecraft.block.Block;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NoSlowdown extends ToggleMod {
    public NoSlowdown(String modName, boolean defaultValue, String description, int key) {
        super(modName, defaultValue, description, key);
    }

    @SubscribeEvent
    public void onDoApplyBlockMovement(DoBlockCollisionsEvent event) {
        if(event.getEntity().equals(getLocalPlayer())) {
            if(Block.getIdFromBlock(event.getState().getBlock()) == 88) { // soul sand
                event.setCanceled(true);
            }
        }
    }
}
