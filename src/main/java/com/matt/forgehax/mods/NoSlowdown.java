package com.matt.forgehax.mods;

import com.matt.forgehax.asm.ForgeHaxHooks;
import com.matt.forgehax.asm.events.DoBlockCollisionsEvent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSoulSand;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NoSlowdown extends ToggleMod {
    public NoSlowdown(String modName, boolean defaultValue, String description, int key) {
        super(modName, defaultValue, description, key);
    }

    @Override
    public void onEnabled() {
        ForgeHaxHooks.isNoSlowDownActivated = true;
        try {
            ForgeHaxHooks.LIST_BLOCK_FILTER.add(BlockSoulSand.class);
        } catch (Exception e) {}
    }

    @Override
    public void onDisabled() {
        ForgeHaxHooks.isNoSlowDownActivated = false;
        try {
            ForgeHaxHooks.LIST_BLOCK_FILTER.remove(BlockSoulSand.class);
        } catch (Exception e) {}
    }

    @SubscribeEvent
    public void onDoApplyBlockMovement(DoBlockCollisionsEvent event) {
        if(event.getEntity().equals(WRAPPER.getLocalPlayer())) {
            if(Block.getIdFromBlock(event.getState().getBlock()) == 88) { // soul sand
                event.setCanceled(true);
            }
        }
    }
}
