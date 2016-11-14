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
    public static final Minecraft MC = FMLClientHandler.instance().getClient();
    public static final ForgeHax MOD = ForgeHax.instance();

    public static final EntityPlayer getLocalPlayer() {
        return MC.thePlayer;
    }

    public static final World getWorld() {
        return MC.theWorld;
    }

    public static final NetworkManager getNetworkManager() {
        return FMLClientHandler.instance().getClientToServerNetworkManager();
    }
}
