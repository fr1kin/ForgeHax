package com.matt.forgehax.mods;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.matt.forgehax.Wrapper.*;

@RegisterMod
public class AutoRespawnMod extends ToggleMod {
    public AutoRespawnMod() {
        super("AutoRespawn", false, "Auto respawn on death");
    }

    @SubscribeEvent
    public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
        if(getLocalPlayer().getHealth() <= 0) {
            getLocalPlayer().respawnPlayer();
        }
    }
}
