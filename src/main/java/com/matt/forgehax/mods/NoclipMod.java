package com.matt.forgehax.mods;

import com.google.common.eventbus.Subscribe;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.matt.forgehax.Helper.*;

@RegisterMod
public class NoclipMod extends ToggleMod {
    public NoclipMod() {
        super(Category.PLAYER, "Noclip", false, "Enables player noclip");
    }

    @Override
    public void onDisabled() {
        Entity local = getRidingOrPlayer();
        if (local != null)
            local.noClip = false;
    }

    @Subscribe
    @SubscribeEvent
    public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
        Entity local = getRidingOrPlayer();
        local.noClip = true;
        local.onGround = false;
        local.fallDistance = 0;
    }
}
