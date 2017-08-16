package com.matt.forgehax.util.entity;

import com.matt.forgehax.Globals;
import com.matt.forgehax.util.container.ContainerManager;
import com.matt.forgehax.util.container.lists.PlayerList;
import net.minecraft.entity.player.EntityPlayer;
import static com.matt.forgehax.Helper.*;

public class PlayerUtils implements Globals {
    public static boolean isLocalPlayer(EntityPlayer player) {
        EntityPlayer localPlayer = getLocalPlayer();
        return localPlayer != null && localPlayer.equals(player);
    }

    public static boolean isFriend(EntityPlayer player) {
        String uuid = player.getUniqueID().toString();
        for(Object o : ContainerManager.getContainerCollection(ContainerManager.Category.PLAYERS)) {
            PlayerList playerList = (PlayerList)o;
            if(playerList.containsPlayer(uuid))
                return true;
        }
        return false;
    }
}
