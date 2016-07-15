package com.matt.forgehax;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetworkManager;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;

/**
 * 2 lazy to import static
 */
public class ForgeHaxBase {
    protected static final Minecraft MC = FMLClientHandler.instance().getClient();
    protected static final ForgeHax MOD = ForgeHax.instance();

    protected static final EntityPlayer getLocalPlayer() {
        return MC.thePlayer;
    }

    protected static final World getWorld() {
        return MC.theWorld;
    }

    protected static final NetworkManager getNetworkManager() {
        return FMLClientHandler.instance().getClientToServerNetworkManager();
    }
}
