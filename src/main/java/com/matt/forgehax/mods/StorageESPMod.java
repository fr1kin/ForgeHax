package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getWorld;

import com.github.lunatrius.core.client.renderer.unique.GeometryMasks;
import com.github.lunatrius.core.client.renderer.unique.GeometryTessellator;
import com.matt.forgehax.events.RenderEvent;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.item.minecart.ChestMinecartEntity;
import net.minecraft.tileentity.*;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;

/** Created on 9/4/2016 by fr1kin */
@RegisterMod
public class StorageESPMod extends ToggleMod {

  public StorageESPMod() {
    super(Category.RENDER, "StorageESP", false, "Shows storage");
  }

  private int getTileEntityColor(TileEntity tileEntity) {
    if (tileEntity instanceof ChestTileEntity
        || tileEntity instanceof DispenserTileEntity
        || tileEntity instanceof ShulkerBoxTileEntity) return Utils.Colors.ORANGE;
    else if (tileEntity instanceof EnderChestTileEntity) return Utils.Colors.PURPLE;
    else if (tileEntity instanceof FurnaceTileEntity) return Utils.Colors.GRAY;
    else if (tileEntity instanceof HopperTileEntity) return Utils.Colors.DARK_RED;
    else return -1;
  }

  private int getEntityColor(Entity entity) {
    if (entity instanceof ChestMinecartEntity) return Utils.Colors.ORANGE;
    else if (entity instanceof ItemFrameEntity
        && Block.getBlockFromItem(((ItemFrameEntity) entity).getDisplayedItem().getItem()) instanceof ShulkerBoxBlock)
      return Utils.Colors.YELLOW;
    else return -1;
  }

  @SubscribeEvent
  public void onRender(RenderEvent event) {
    event.getBuffer().begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

    for (TileEntity tileEntity : getWorld().loadedTileEntityList) {
      BlockPos pos = tileEntity.getPos();

      int color = getTileEntityColor(tileEntity);
      if (color != -1)
        GeometryTessellator.drawCuboid(event.getBuffer(), pos, GeometryMasks.Line.ALL, color);
    }

    for (Entity entity : getWorld().func_217416_b()) {
      BlockPos pos = entity.getPosition();
      int color = getEntityColor(entity);
      if (color != -1)
        GeometryTessellator.drawCuboid(
            event.getBuffer(),
            entity instanceof ItemFrameEntity ? pos.add(0, -1, 0) : pos,
            GeometryMasks.Line.ALL,
            color);
    }

    event.getTessellator().draw();
  }
}
