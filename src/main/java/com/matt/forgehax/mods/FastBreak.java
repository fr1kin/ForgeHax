package com.matt.forgehax.mods;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class FastBreak extends ToggleMod {
    public FastBreak() {
        super("FastBreak", false, "Fast break retard");
    }

    @SubscribeEvent
    public void onUpdate(LocalPlayerUpdateEvent event) {
        if(MC.playerController != null)
            MC.playerController.blockHitDelay = 0;
    }
}
