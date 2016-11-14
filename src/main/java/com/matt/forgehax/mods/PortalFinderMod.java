package com.matt.forgehax.mods;

import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.draw.RenderUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEndGateway;
import net.minecraft.tileentity.TileEntityEndPortal;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created on 11/10/2016 by fr1kin
 */
public class PortalFinderMod extends ToggleMod {
    public PortalFinderMod(String modName, boolean defaultValue, String description, int key) {
        super(modName, defaultValue, description, key);
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        for (TileEntity tileEntity : getWorld().loadedTileEntityList) {
            BlockPos pos = tileEntity.getPos();
            if (tileEntity instanceof TileEntityEndGateway) {
                RenderUtils.drawBox(
                        pos,
                        pos.add(1, 1, 1),
                        Utils.Colors.GREEN,
                        2,
                        true
                );
            }
        }
    }
}