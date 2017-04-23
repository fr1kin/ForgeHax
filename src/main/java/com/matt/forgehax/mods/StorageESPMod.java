package com.matt.forgehax.mods;

import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.draw.RenderUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityMinecartChest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.tileentity.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created on 9/4/2016 by fr1kin
 */
public class StorageESPMod extends ToggleMod {

    public StorageESPMod() {
        super("StorageESP", false, "Shows storage");
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        for(TileEntity tileEntity : WRAPPER.getWorld().loadedTileEntityList) {
            BlockPos pos = tileEntity.getPos();
            if(tileEntity instanceof TileEntityChest || tileEntity instanceof TileEntityDispenser || tileEntity instanceof TileEntityShulkerBox) {
                RenderUtils.drawBox(
                        pos,
                        pos.add(1, 1, 1),
                        Utils.Colors.ORANGE,
                        2,
                        true
                );
            } else if(tileEntity instanceof TileEntityEnderChest) {
                RenderUtils.drawBox(
                        pos,
                        pos.add(1, 1, 1),
                        Utils.Colors.PURPLE,
                        2,
                        true
                );
            } else if(tileEntity instanceof TileEntityFurnace) {
                RenderUtils.drawBox(
                        pos,
                        pos.add(1, 1, 1),
                        Utils.Colors.GRAY,
                        2,
                        true
                );
            } else if(tileEntity instanceof TileEntityHopper) {
                RenderUtils.drawBox(
                        pos,
                        pos.add(1, 1, 1),
                        Utils.Colors.DARK_RED,
                        2,
                        true
                );
            }
        }
        for(Entity entity : WRAPPER.getWorld().loadedEntityList) {
            if(entity instanceof EntityMinecartChest) {
                BlockPos pos = entity.getPosition();
                RenderUtils.drawBox(
                        pos,
                        pos.add(1, 1, 1),
                        Utils.Colors.ORANGE,
                        2,
                        true
                );
            } else if(entity instanceof EntityItemFrame &&
                    ((EntityItemFrame) entity).getDisplayedItem().getItem() instanceof ItemShulkerBox) {
                BlockPos pos = entity.getPosition();
                RenderUtils.drawBox(
                        pos,
                        pos.add(1, -1, 1),
                        Utils.Colors.YELLOW,
                        2,
                        true
                );
            }
        }
    }
}
