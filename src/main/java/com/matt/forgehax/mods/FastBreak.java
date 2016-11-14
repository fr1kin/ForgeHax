package com.matt.forgehax.mods;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class FastBreak extends ToggleMod {
    public FastBreak(String modName, boolean defaultValue, String description, int key) {
        super(modName, defaultValue, description, key);
    }

    @SubscribeEvent
    public void onUpdate(LocalPlayerUpdateEvent event) {
        if(MC.playerController != null)
            MC.playerController.blockHitDelay = 0;
    }
}
