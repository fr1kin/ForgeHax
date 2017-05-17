package com.matt.forgehax.mods;

import com.matt.forgehax.asm.events.BlockRenderEvent;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.draw.RenderUtils;
import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockPortal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEndGateway;
import net.minecraft.tileentity.TileEntityEndPortal;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import static com.matt.forgehax.Wrapper.*;

/**
 * Created on 11/10/2016 by fr1kin
 */

@RegisterMod
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

    @SubscribeEvent
    public void onBlockRender(BlockRenderEvent event) {
        Block block = event.getState().getBlock();
        if(block instanceof BlockPortal) {
            AxisAlignedBB bb = event.getState().getBoundingBox(event.getAccess(), event.getPos());

        }
    }
}