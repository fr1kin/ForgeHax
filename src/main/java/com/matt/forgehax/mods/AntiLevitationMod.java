package com.matt.forgehax.mods;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import net.minecraft.potion.Potion;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created on 11/28/2016 by fr1kin
 */
public class AntiLevitationMod extends ToggleMod {
    public AntiLevitationMod() {
        super("AntiLevitation", false, "No levitation");
    }

    @SubscribeEvent
    public void onUpdate(LocalPlayerUpdateEvent event) {
        if(WRAPPER.getLocalPlayer().isPotionActive(Potion.getPotionFromResourceLocation("levitation"))) {
            WRAPPER.getLocalPlayer().removeActivePotionEffect(Potion.getPotionFromResourceLocation("levitation"));
        }
    }
}
