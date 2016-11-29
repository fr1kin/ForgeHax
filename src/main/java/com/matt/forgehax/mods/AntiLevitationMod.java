package com.matt.forgehax.mods;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import net.minecraft.potion.Potion;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created on 11/28/2016 by fr1kin
 */
public class AntiLevitationMod extends ToggleMod {
    public AntiLevitationMod(String modName, boolean defaultValue, String description, int key) {
        super(modName, defaultValue, description, key);
    }

    @SubscribeEvent
    public void onUpdate(LocalPlayerUpdateEvent event) {
        if(getLocalPlayer().isPotionActive(Potion.getPotionFromResourceLocation("levitation"))) {
            getLocalPlayer().removeActivePotionEffect(Potion.getPotionFromResourceLocation("levitation"));
        }
    }
}
