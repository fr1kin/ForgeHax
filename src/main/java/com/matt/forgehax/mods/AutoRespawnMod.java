package com.matt.forgehax.mods;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import static com.matt.forgehax.Wrapper.*;

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
