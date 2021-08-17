package dev.fiki.forgehax.main.mods.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.fiki.forgehax.api.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.api.cmd.settings.ColorSetting;
import dev.fiki.forgehax.api.color.Color;
import dev.fiki.forgehax.api.color.Colors;
import dev.fiki.forgehax.api.draw.GeometryMasks;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.render.RenderSpaceEvent;
import dev.fiki.forgehax.api.extension.EntityEx;
import dev.fiki.forgehax.api.extension.VectorEx;
import dev.fiki.forgehax.api.extension.VertexBuilderEx;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import lombok.experimental.ExtensionMethod;
import lombok.val;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.item.minecart.ChestMinecartEntity;
import net.minecraft.entity.item.minecart.FurnaceMinecartEntity;
import net.minecraft.entity.item.minecart.HopperMinecartEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.tileentity.*;
import net.minecraft.util.math.shapes.VoxelShape;
import org.lwjgl.opengl.GL11;

import static dev.fiki.forgehax.main.Common.getWorld;

@RegisterMod(
    name = "StorageESP",
    description = "Shows storage",
    category = Category.RENDER
)
@ExtensionMethod({EntityEx.class, VectorEx.class, VertexBuilderEx.class})
public class StorageESPMod extends ToggleMod {
  private final ColorSetting chestColor = newColorSetting()
      .name("chest-color")
      .description("Color for Chests")
      .defaultTo(Colors.ORANGE)
      .build();

  private final ColorSetting dispenserColor = newColorSetting()
      .name("dispenser-color")
      .description("Color for Dispensers")
      .defaultTo(Colors.ORANGE)
      .build();

  private final ColorSetting shulkerBoxColor = newColorSetting()
      .name("shulker-color")
      .description("Color for Dispensers")
      .defaultTo(Colors.YELLOW)
      .build();

  private final ColorSetting enderChestColor = newColorSetting()
      .name("enderchest-color")
      .description("Color for Ender Chests")
      .defaultTo(Colors.PURPLE)
      .build();

  private final ColorSetting furnaceColor = newColorSetting()
      .name("furnace-color")
      .description("Color for Furnaces")
      .defaultTo(Colors.GRAY)
      .build();

  private final ColorSetting hopperColor = newColorSetting()
      .name("hopper-color")
      .description("Color for Hoppers")
      .defaultTo(Colors.GRAY)
      .build();

  private final BooleanSetting antiAliasing = newBooleanSetting()
      .name("anti-aliasing")
      .description("Makes lines appear smoother. May impact framerate significantly")
      .defaultTo(false)
      .build();

  private Color getTileEntityColor(TileEntity te) {
    if (te instanceof ChestTileEntity) {
      return chestColor.getValue();
    } else if (te instanceof DispenserTileEntity) {
      return dispenserColor.getValue();
    } else if (te instanceof ShulkerBoxTileEntity) {
      return shulkerBoxColor.getValue();
    } else if (te instanceof EnderChestTileEntity) {
      return enderChestColor.getValue();
    } else if (te instanceof FurnaceTileEntity) {
      return furnaceColor.getValue();
    } else if (te instanceof HopperTileEntity) {
      return hopperColor.getValue();
    }
    return null;
  }

  private Color getEntityColor(Entity e) {
    if (e instanceof ChestMinecartEntity) {
      return chestColor.getValue();
    } else if (e instanceof FurnaceMinecartEntity) {
      return furnaceColor.getValue();
    } else if (e instanceof HopperMinecartEntity) {
      return hopperColor.getValue();
    } else if (e instanceof ItemFrameEntity
        && ((ItemFrameEntity) e).getItem().getItem() instanceof BlockItem
        && ((BlockItem) ((ItemFrameEntity) e).getItem().getItem()).getBlock() instanceof ShulkerBoxBlock) {
      return shulkerBoxColor.getValue();
    }
    return null;
  }

  @SubscribeListener
  public void onRender(RenderSpaceEvent event) {
    val stack = event.getStack();
    val buffer = event.getBuffer();
    stack.pushPose();
    stack.translateVec(event.getProjectedPos().scale(-1));

    buffer.beginLines(DefaultVertexFormats.POSITION_COLOR);

    for (TileEntity ent : getWorld().tickableBlockEntities) {
      Color color = getTileEntityColor(ent);
      if (color != null && color.getAlpha() > 0) {
        BlockState state = ent.getBlockState();
        VoxelShape voxel = state.getCollisionShape(getWorld(), ent.getBlockPos());
        if (!voxel.isEmpty()) {
          buffer.outlinedCube(voxel.bounds().move(ent.getBlockPos()),
              GeometryMasks.Line.ALL, color, stack.getLastMatrix());
        }
      }
    }

    for (Entity ent : getWorld().entitiesForRendering()) {
      Color color = getEntityColor(ent);
      if (color != null && color.getAlpha() > 0) {
        buffer.outlinedCube(ent.getBoundingBox()
                .expandTowards(ent.position().scale(-1D))
                .expandTowards(ent.getInterpolatedPos(event.getPartialTicks())),
            GeometryMasks.Line.ALL, color, stack.getLastMatrix());
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
