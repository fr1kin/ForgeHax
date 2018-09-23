package com.matt.forgehax.mods;

import com.google.common.eventbus.Subscribe;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.matt.forgehax.Helper.getLocalPlayer;

@RegisterMod
public class AutoRespawnMod extends ToggleMod {
    public AutoRespawnMod() {
        super(Category.PLAYER, "AutoRespawn", false, "Auto respawn on death");
    }

    @Subscribe
    @SubscribeEvent
    public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
        if(getLocalPlayer().getHealth() <= 0) {
            getLocalPlayer().respawnPlayer();
        }
    }
}
