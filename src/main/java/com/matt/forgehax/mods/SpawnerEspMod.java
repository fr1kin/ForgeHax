package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getWorld;

import com.github.lunatrius.core.client.renderer.unique.GeometryMasks;
import com.github.lunatrius.core.client.renderer.unique.GeometryTessellator;
import com.matt.forgehax.events.RenderEvent;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

/** Created on 9/29/2016 by fr1kin */
@RegisterMod
public class SpawnerEspMod extends ToggleMod {
  public SpawnerEspMod() {
    super(Category.RENDER, "SpawnerESP", false, "Spawner esp");
  }

  @SubscribeEvent
  public void onRender(RenderEvent event) {
    event.getBuffer().begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

    for (TileEntity tileEntity : getWorld().loadedTileEntityList) {
      if (tileEntity instanceof TileEntityMobSpawner) {
        BlockPos pos = tileEntity.getPos();
        GeometryTessellator.drawCuboid(
            event.getBuffer(), pos, GeometryMasks.Line.ALL, Utils.Colors.RED);
      }
    }

    event.getTessellator().draw();
  }
}
