package com.matt.forgehax.mods;

import com.github.lunatrius.core.client.renderer.GeometryMasks;
import com.github.lunatrius.core.client.renderer.GeometryTessellator;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.draw.RenderUtils;
import com.matt.forgehax.util.entity.EntityUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import static com.matt.forgehax.Wrapper.*;

/**
 * Created on 9/29/2016 by fr1kin
 */
public class SpawnerEspMod extends ToggleMod {
    public SpawnerEspMod() {
        super("SpawnerESP", false, "Spawner esp");
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        final Tessellator tessellator = Tessellator.getInstance();

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.disableDepth();

        GlStateManager.glLineWidth(1.f);

        Vec3d renderPos = EntityUtils.getInterpolatedPos(getLocalPlayer(), event.getPartialTicks());
        GlStateManager.translate(-renderPos.xCoord, -renderPos.yCoord, -renderPos.zCoord);

        tessellator.getBuffer().begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        for(TileEntity tileEntity : getWorld().loadedTileEntityList) {
            if(tileEntity instanceof TileEntityMobSpawner) {
                BlockPos pos = tileEntity.getPos();
                GeometryTessellator.drawCuboid(tessellator.getBuffer(), pos, GeometryMasks.Line.ALL, Utils.Colors.RED);
            }
        }

        tessellator.draw();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.enableCull();
        GlStateManager.popMatrix();
    }
}
