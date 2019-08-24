package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getWorld;

import com.matt.forgehax.events.RenderEvent;
import com.matt.forgehax.util.color.Colors;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.matt.forgehax.util.tesselation.GeometryMasks;
import com.matt.forgehax.util.tesselation.GeometryTessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityMinecartChest;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

/**
 * Created on 9/4/2016 by fr1kin
 */
@RegisterMod
public class StorageESPMod extends ToggleMod {
  
  public StorageESPMod() {
    super(Category.RENDER, "StorageESP", false, "Shows storage");
  }
  
  private int getTileEntityColor(TileEntity tileEntity) {
    if (tileEntity instanceof TileEntityChest
      || tileEntity instanceof TileEntityDispenser
      || tileEntity instanceof TileEntityShulkerBox) {
      return Colors.ORANGE.toBuffer();
    } else if (tileEntity instanceof TileEntityEnderChest) {
      return Colors.PURPLE.toBuffer();
    } else if (tileEntity instanceof TileEntityFurnace) {
      return Colors.GRAY.toBuffer();
    } else if (tileEntity instanceof TileEntityHopper) {
      return Colors.DARK_RED.toBuffer();
    } else {
      return -1;
    }
  }
  
  private int getEntityColor(Entity entity) {
    if (entity instanceof EntityMinecartChest) {
      return Colors.ORANGE.toBuffer();
    } else if (entity instanceof EntityItemFrame
      && ((EntityItemFrame) entity).getDisplayedItem().getItem() instanceof ItemShulkerBox) {
      return Colors.YELLOW.toBuffer();
    } else {
      return -1;
    }
  }
  
  @SubscribeEvent
  public void onRender(RenderEvent event) {
    event.getBuffer().begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
  
    for (TileEntity tileEntity : getWorld().loadedTileEntityList) {
      BlockPos pos = tileEntity.getPos();
    
      int color = getTileEntityColor(tileEntity);
      if (color != -1) {
        GeometryTessellator.drawCuboid(event.getBuffer(), pos, GeometryMasks.Line.ALL, color);
      }
    }
    
    for (Entity entity : getWorld().loadedEntityList) {
      BlockPos pos = entity.getPosition();
      int color = getEntityColor(entity);
      if (color != -1) {
        GeometryTessellator.drawCuboid(
          event.getBuffer(),
          entity instanceof EntityItemFrame ? pos.add(0, -1, 0) : pos,
          GeometryMasks.Line.ALL,
          color);
      }
    }
    
    event.getTessellator().draw();
  }
}
