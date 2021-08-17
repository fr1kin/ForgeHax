package dev.fiki.forgehax.main.mods.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.fiki.forgehax.api.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.api.cmd.settings.ColorSetting;
import dev.fiki.forgehax.api.color.Colors;
import dev.fiki.forgehax.api.draw.GeometryMasks;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.render.RenderSpaceEvent;
import dev.fiki.forgehax.api.extension.VectorEx;
import dev.fiki.forgehax.api.extension.VertexBuilderEx;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import lombok.experimental.ExtensionMethod;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.shapes.VoxelShape;
import org.lwjgl.opengl.GL11;

import static dev.fiki.forgehax.main.Common.getWorld;

@RegisterMod(
    name = "SpawnerESP",
    description = "Spawner ESP",
    category = Category.RENDER
)
@ExtensionMethod({VectorEx.class, VertexBuilderEx.class})
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

  @SubscribeListener
  public void onRender(RenderSpaceEvent event) {
    if (spawnerColor.getValue().getAlpha() <= 0) {
      return;
    }

    MatrixStack stack = event.getStack();
    BufferBuilder buffer = event.getBuffer();
    stack.pushPose();
    stack.translateVec(event.getProjectedPos().scale(-1));

    buffer.beginLines(DefaultVertexFormats.POSITION_COLOR);

    for (TileEntity ent : getWorld().tickableBlockEntities) {
      if (ent instanceof MobSpawnerTileEntity) {
        BlockState state = ent.getBlockState();
        VoxelShape voxel = state.getCollisionShape(getWorld(), ent.getBlockPos());
        if (!voxel.isEmpty()) {
          buffer.outlinedCube(voxel.bounds().move(ent.getBlockPos()),
              GeometryMasks.Line.ALL, spawnerColor.getValue(), stack.getLastMatrix());
        }
      }
    }

    RenderSystem.enableBlend();
    if (antiAliasing.getValue()) {
      GL11.glEnable(GL11.GL_LINE_SMOOTH);
    }

    buffer.draw();
    GL11.glDisable(GL11.GL_LINE_SMOOTH);
    stack.popPose();
  }
}
