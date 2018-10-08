package com.matt.forgehax.mods;

import com.google.common.eventbus.Subscribe;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.potion.Potion;

import static com.matt.forgehax.Helper.getLocalPlayer;

/**
 * Created on 11/28/2016 by fr1kin
 */

@RegisterMod
public class AntiLevitationMod extends ToggleMod {
    public AntiLevitationMod() {
        super(Category.PLAYER, "AntiLevitation", false, "No levitation");
    }

    @Subscribe
    public void onUpdate(LocalPlayerUpdateEvent event) {
        if(getLocalPlayer().isPotionActive(Potion.getPotionFromResourceLocation("levitation"))) {
            getLocalPlayer().removeActivePotionEffect(Potion.getPotionFromResourceLocation("levitation"));
        }
    }
}
