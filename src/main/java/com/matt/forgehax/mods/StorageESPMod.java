package com.matt.forgehax.mods;

import com.github.lunatrius.core.client.renderer.GeometryMasks;
import com.github.lunatrius.core.client.renderer.GeometryTessellator;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.draw.RenderUtils;
import com.matt.forgehax.util.entity.EntityUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
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
import org.lwjgl.opengl.GL11;

import static com.matt.forgehax.Wrapper.*;

/**
 * Created on 9/4/2016 by fr1kin
 */
public class StorageESPMod extends ToggleMod {

    public StorageESPMod() {
        super("StorageESP", false, "Shows storage");
    }

    private int getTileEntityColor(TileEntity tileEntity) {
        if(tileEntity instanceof TileEntityChest || tileEntity instanceof TileEntityDispenser || tileEntity instanceof TileEntityShulkerBox)
            return Utils.Colors.ORANGE;
        else if(tileEntity instanceof TileEntityEnderChest)
            return Utils.Colors.PURPLE;
        else if(tileEntity instanceof TileEntityFurnace)
            return Utils.Colors.GRAY;
        else if(tileEntity instanceof TileEntityHopper)
            return Utils.Colors.DARK_RED;
        else
            return -1;
    }

    private int getEntityColor(Entity entity) {
        if(entity instanceof EntityMinecartChest)
            return Utils.Colors.ORANGE;
        else if(entity instanceof EntityItemFrame &&
                ((EntityItemFrame) entity).getDisplayedItem().getItem() instanceof ItemShulkerBox)
            return Utils.Colors.YELLOW;
        else
            return -1;
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
            BlockPos pos = tileEntity.getPos();
            int color = getTileEntityColor(tileEntity);
            if(color != -1) GeometryTessellator.drawCuboid(tessellator.getBuffer(), pos, GeometryMasks.Line.ALL, color);
        }

        for(Entity entity : getWorld().loadedEntityList) {
            BlockPos pos = entity.getPosition();
            int color = getEntityColor(entity);
            if(color != -1) GeometryTessellator.drawCuboid(tessellator.getBuffer(), entity instanceof EntityItemFrame ? pos.add(0, -1, 0) : pos, GeometryMasks.Line.ALL, color);
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
