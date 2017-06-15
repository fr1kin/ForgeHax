package com.matt.forgehax.mods;

import com.github.lunatrius.core.client.renderer.GeometryMasks;
import com.github.lunatrius.core.client.renderer.GeometryTessellator;
import com.matt.forgehax.events.RenderEvent;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.*;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import static com.matt.forgehax.Helper.*;

/**
 * Created on 9/29/2016 by fr1kin
 */

@RegisterMod
public class SpawnerEspMod extends ToggleMod {
    public SpawnerEspMod() {
        super("SpawnerESP", false, "Spawner esp");
    }

    @SubscribeEvent
    public void onRender(RenderEvent event) {
        event.getBuffer().begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        for(TileEntity tileEntity : getWorld().loadedTileEntityList) {
            if(tileEntity instanceof TileEntityMobSpawner) {
                BlockPos pos = tileEntity.getPos();
                GeometryTessellator.drawCuboid(event.getBuffer(), pos, GeometryMasks.Line.ALL, Utils.Colors.RED);
            }
        }

        event.getTessellator().draw();
    }
}
