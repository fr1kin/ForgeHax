package com.matt.forgehax.mods;

import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.draw.RenderUtils;
import net.minecraft.tileentity.*;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created on 9/29/2016 by fr1kin
 */
public class SpawnerEspMod extends ToggleMod {
    public SpawnerEspMod(String modName, boolean defaultValue, String description, int key) {
        super(modName, defaultValue, description, key);
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        for (TileEntity tileEntity : getWorld().loadedTileEntityList) {
            BlockPos pos = tileEntity.getPos();
            if (tileEntity instanceof TileEntityMobSpawner) {
                RenderUtils.drawBox(
                        pos,
                        pos.add(1, 1, 1),
                        Utils.Colors.RED,
                        2,
                        true
                );
            }
        }
    }
}
