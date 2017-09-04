package com.matt.forgehax.mods;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.matt.forgehax.Helper.getLocalPlayer;

@RegisterMod
public class NoclipMod extends ToggleMod {
    public NoclipMod() {
        super(Category.PLAYER, "Noclip", false, "Enables player noclip");
    }

    @Override
    public void onDisabled() {
        if(getLocalPlayer() != null)
            getLocalPlayer().noClip = false;
    }

    @SubscribeEvent
    public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
        EntityPlayer localPlayer = getLocalPlayer();
        localPlayer.noClip = true;
        localPlayer.onGround = false;
        localPlayer.fallDistance = 0;
    }
}
