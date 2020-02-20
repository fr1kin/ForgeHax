package dev.fiki.forgehax.main.mods;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.fiki.forgehax.main.events.RenderEvent;
import dev.fiki.forgehax.main.util.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.main.util.cmd.settings.ColorSetting;
import dev.fiki.forgehax.main.util.color.Colors;
import dev.fiki.forgehax.main.util.draw.BufferBuilderEx;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.util.draw.GeometryMasks;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.util.math.shapes.VoxelShape;
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

  private final BooleanSetting antiAliasing = newBooleanSetting()
      .name("anti-aliasing")
      .description("Makes lines appear smoother. May impact framerate significantly")
      .defaultTo(false)
      .build();

  public SpawnerEspMod() {
    super(Category.RENDER, "SpawnerESP", false, "Spawner esp");
  }

  @SubscribeEvent
  public void onRender(RenderEvent event) {
    if(spawnerColor.getValue().getAlpha() <= 0) {
      return;
    }

    BufferBuilderEx buffer = event.getBuffer();
    buffer.beginLines(DefaultVertexFormats.POSITION_COLOR);

    buffer.setTranslation(event.getProjectedPos().scale(-1));

    worldTileEntities()
        .filter(MobSpawnerTileEntity.class::isInstance)
        .forEach(ent -> {
          BlockState state = ent.getBlockState();
          VoxelShape voxel = state.getCollisionShape(getWorld(), ent.getPos());
          if(!voxel.isEmpty()) {
            buffer.putOutlinedCuboid(voxel.getBoundingBox().offset(ent.getPos()),
                GeometryMasks.Line.ALL, spawnerColor.getValue());
          }
        });

    RenderSystem.enableBlend();
    if(antiAliasing.getValue()) {
      GL11.glEnable(GL11.GL_LINE_SMOOTH);
    }

    buffer.draw();

    GL11.glDisable(GL11.GL_LINE_SMOOTH);
  }
}
