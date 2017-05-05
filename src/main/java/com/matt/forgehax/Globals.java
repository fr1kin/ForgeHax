package com.matt.forgehax;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.NetworkManager;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;

/**
 * 2 lazy to import static
 */
public interface Globals {
    Minecraft MC            = FMLClientHandler.instance().getClient();
    ForgeHax MOD            = ForgeHax.getInstance();
}
