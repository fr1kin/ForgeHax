package com.matt.forgehax.util.entity;

import com.matt.forgehax.Globals;
import net.minecraft.entity.player.EntityPlayer;

import static com.matt.forgehax.Helper.getLocalPlayer;

public class PlayerUtils implements Globals {
    public static boolean isLocalPlayer(EntityPlayer player) {
        EntityPlayer localPlayer = getLocalPlayer();
        return localPlayer != null && localPlayer.equals(player);
    }

    public static boolean isFriend(EntityPlayer player) {
        return false;
    }
}
