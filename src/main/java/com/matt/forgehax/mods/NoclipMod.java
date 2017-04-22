package com.matt.forgehax.mods;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class NoclipMod extends ToggleMod {
    public NoclipMod(String modName, boolean defaultValue, String description, int key) {
        super(modName, defaultValue, description, key);
    }

    @Override
    public void onDisabled() {
        if(WRAPPER.getLocalPlayer() != null)
            WRAPPER.getLocalPlayer().noClip = false;
    }

    @SubscribeEvent
    public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
        EntityPlayer localPlayer = WRAPPER.getLocalPlayer();
        localPlayer.noClip = true;
        localPlayer.onGround = false;
        localPlayer.fallDistance = 0;
    }
}
