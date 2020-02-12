package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.events.RenderEvent;
import dev.fiki.forgehax.main.util.cmd.settings.ColorSetting;
import dev.fiki.forgehax.main.util.color.Colors;
import dev.fiki.forgehax.main.util.draw.BufferBuilderEx;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.util.draw.GeometryMasks;
import dev.fiki.forgehax.main.util.tesselation.GeometryTessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import static dev.fiki.forgehax.main.Common.*;

/**
 * Created on 9/29/2016 by fr1kin
 */
@RegisterMod
public class SpawnerEspMod extends ToggleMod {
  private final ColorSetting spawnerColor = newColorSetting()
      .name("spawner-color")
      .description("Color for Spawners")
      .defaultTo(Colors.RED)
      .build();

  public SpawnerEspMod() {
    super(Category.RENDER, "SpawnerESP", false, "Spawner esp");
  }

  @SubscribeEvent
  public void onRender(RenderEvent event) {
    BufferBuilderEx buffer = event.getBuffer();
    buffer.beginLines(DefaultVertexFormats.POSITION_COLOR);

    worldTileEntities()
        .filter(MobSpawnerTileEntity.class::isInstance)
        .forEach(ent -> buffer.appendOutlinedCuboid(ent.getRenderBoundingBox(),
            GeometryMasks.Line.ALL, spawnerColor.getValue()));

    buffer.draw();
  }
}
