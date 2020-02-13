package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.events.RenderEvent;
import dev.fiki.forgehax.main.util.cmd.settings.ColorSetting;
import dev.fiki.forgehax.main.util.color.Color;
import dev.fiki.forgehax.main.util.color.Colors;
import dev.fiki.forgehax.main.util.draw.BufferBuilderEx;
import dev.fiki.forgehax.main.util.entity.EntityUtils;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.util.draw.GeometryMasks;
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
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static dev.fiki.forgehax.main.Common.*;

/**
 * Created on 9/4/2016 by fr1kin
 */
@RegisterMod
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

  public StorageESPMod() {
    super(Category.RENDER, "StorageESP", false, "Shows storage");
  }

  private Color getTileEntityColor(TileEntity te) {
    if (te instanceof ChestTileEntity) {
      return chestColor.getValue();
    } else if(te instanceof DispenserTileEntity) {
      return dispenserColor.getValue();
    } else if(te instanceof ShulkerBoxTileEntity) {
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
    } else if(e instanceof FurnaceMinecartEntity) {
      return furnaceColor.getValue();
    } else if(e instanceof HopperMinecartEntity) {
      return hopperColor.getValue();
    } else if (e instanceof ItemFrameEntity
        && ((ItemFrameEntity) e).getDisplayedItem().getItem() instanceof BlockItem
        && ((BlockItem) ((ItemFrameEntity) e).getDisplayedItem().getItem()).getBlock() instanceof ShulkerBoxBlock) {
      return shulkerBoxColor.getValue();
    }
    return null;
  }

  @SubscribeEvent
  public void onRender(RenderEvent event) {
    BufferBuilderEx buffer = event.getBuffer();
    buffer.beginLines(DefaultVertexFormats.POSITION_COLOR);

    buffer.setTranslation(event.getProjectedPos().scale(-1));

    worldTileEntities().forEach(ent -> {
      Color color = getTileEntityColor(ent);
      if(color != null && color.getAlpha() > 0) {
        BlockState state = ent.getBlockState();
        VoxelShape voxel = state.getCollisionShape(getWorld(), ent.getPos());
        if(!voxel.isEmpty()) {
          buffer.appendOutlinedCuboid(voxel.getBoundingBox().offset(ent.getPos()), GeometryMasks.Line.ALL, color);
        }
      }
    });

    worldEntities().forEach(ent -> {
      Color color = getEntityColor(ent);
      if(color != null && color.getAlpha() > 0) {
        buffer.appendOutlinedCuboid(ent.getBoundingBox()
            .offset(ent.getPositionVector().scale(-1D))
            .offset(EntityUtils.getInterpolatedPos(ent, event.getPartialTicks())),
            GeometryMasks.Line.ALL, color);
      }
    });

    buffer.draw();
  }
}
