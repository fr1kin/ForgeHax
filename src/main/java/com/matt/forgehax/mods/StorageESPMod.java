package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getWorld;

import com.matt.forgehax.events.RenderEvent;
import com.matt.forgehax.util.color.Color;
import com.matt.forgehax.util.color.Colors;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.matt.forgehax.util.tesselation.GeometryMasks;
import com.matt.forgehax.util.tesselation.GeometryTessellator;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityMinecartChest;
import net.minecraft.entity.item.EntityMinecartHopper;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.tileentity.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import org.lwjgl.opengl.GL11;

/**
 * Created on 9/4/2016 by fr1kin
 * Updated by OverFloyd, may 2020
 */
@RegisterMod
public class StorageESPMod extends ToggleMod {

  public final Setting<Boolean> outline =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("outline")
          .description("Renders an outline around the block")
          .defaultTo(true)
          .build();

  public final Setting<Boolean> fill =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("fill")
          .description("Renders a box on the block")
          .defaultTo(true)
          .build();

  public final Setting<Integer> alpha =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("alpha")
          .description("Alpha value for fill mode")
          .defaultTo(64)
          .min(0)
          .max(255)
          .build();

  private final Setting<Float> width =
      getCommandStub()
          .builders()
          .<Float>newSettingBuilder()
          .name("width")
          .description("The width value for the outline")
          .min(0.5f)
          .defaultTo(1.0f)
          .build();

  public final Setting<Boolean> antialias =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("antialias")
          .description("Makes lines and triangles more smooth, may hurt performance")
          .defaultTo(true)
          .build();

  public final Setting<Boolean> chests =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("chests")
          .description("Show chests")
          .defaultTo(true)
          .build();

  public final Setting<Boolean> dispensers =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("dispensers")
          .description("Show dispensers and droppers (parent)")
          .defaultTo(true)
          .build();

  public final Setting<Boolean> shulkers =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("shulkers")
          .description("Show shulker boxes")
          .defaultTo(true)
          .build();

  public final Setting<Boolean> eChests =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("ender")
          .description("Show ender chests")
          .defaultTo(true)
          .build();

  public final Setting<Boolean> furnaces =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("furnaces")
          .description("Show furnaces")
          .defaultTo(true)
          .build();

  public final Setting<Boolean> hoppers =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("hoppers")
          .description("Show hoppers")
          .defaultTo(true)
          .build();

  public final Setting<Boolean> itemFrames =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("item-frames")
          .description("Show item frames")
          .defaultTo(true)
          .build();

  public StorageESPMod() {
    super(Category.RENDER, "StorageESP", false, "Shows storage blocks/entities");
  }

  private int count = 0;

  @Override
  public String getDisplayText() {
    return (getModName() + " [" + count + "]");
  }

  private int getTileEntityOutlineColor(TileEntity tileEntity) {
    if (chests.getAsBoolean() && tileEntity instanceof TileEntityChest) {
      return Colors.ORANGE.toBuffer();
    } else if (dispensers.getAsBoolean() && tileEntity instanceof TileEntityDispenser) {
      if(tileEntity instanceof TileEntityDropper) {
        return Colors.GREEN.toBuffer();
      }
      return Colors.DARK_GREEN.toBuffer();
    } else if (shulkers.getAsBoolean() && tileEntity instanceof TileEntityShulkerBox) {
      return Colors.GOLD.toBuffer();
    } else if (eChests.getAsBoolean() && tileEntity instanceof TileEntityEnderChest) {
      return Colors.PURPLE.toBuffer();
    } else if (furnaces.getAsBoolean() && tileEntity instanceof TileEntityFurnace) {
      return Colors.GRAY.toBuffer();
    } else if (hoppers.getAsBoolean() && tileEntity instanceof TileEntityHopper) {
      return Colors.DARK_RED.toBuffer();
    } else return -1;
  }

  private int getTileEntityFillColor(TileEntity tileEntity) {
    if (chests.getAsBoolean() && tileEntity instanceof TileEntityChest) {
      return Color.of(255, 128, 0, alpha.get()).toBuffer(); //ORANGE
    } else if (dispensers.getAsBoolean() && tileEntity instanceof TileEntityDispenser) {
      if (tileEntity instanceof TileEntityDropper) {
        return Color.of(0, 255, 0, alpha.get()).toBuffer(); //GREEN
      }
      return Color.of(0, 170, 0, alpha.get()).toBuffer(); //DARK_GREEN
    } else if (shulkers.getAsBoolean() && tileEntity instanceof TileEntityShulkerBox) {
      return Color.of(255, 191, 0, alpha.get()).toBuffer(); //GOLD
    } else if (eChests.getAsBoolean() && tileEntity instanceof TileEntityEnderChest) {
      return Color.of(163, 73, 163, alpha.get()).toBuffer(); //PURPLE
    } else if (furnaces.getAsBoolean() && tileEntity instanceof TileEntityFurnace) {
      return Color.of(128, 128, 128, alpha.get()).toBuffer(); //GRAY
    } else if (hoppers.getAsBoolean() && tileEntity instanceof TileEntityHopper) {
      return Color.of(128, 0, 0, alpha.get()).toBuffer(); //DARK_RED
    } else return -1;
  }

  private int getEntityOutlineColor(Entity entity) {
    if (chests.getAsBoolean() && entity instanceof EntityMinecartChest)
      return Colors.ORANGE.toBuffer();
    else if (hoppers.getAsBoolean() && entity instanceof EntityMinecartHopper)
      return Colors.DARK_RED.toBuffer();
    else if (itemFrames.getAsBoolean() && entity instanceof EntityItemFrame
        && ((EntityItemFrame) entity).getDisplayedItem().getItem() instanceof ItemShulkerBox)
      return Colors.GOLD.toBuffer();
    else if (itemFrames.getAsBoolean() && entity instanceof EntityItemFrame
        && !(((EntityItemFrame) entity).getDisplayedItem().getItem() instanceof ItemShulkerBox))
      return Colors.BROWN.toBuffer();

    else return -1;
  }

  private int getEntityFillColor(Entity entity) {
    if (chests.getAsBoolean() && entity instanceof EntityMinecartChest)
      return Color.of(255, 128, 0, alpha.get()).toBuffer(); //ORANGE
    else if (hoppers.getAsBoolean() && entity instanceof EntityMinecartHopper)
      return Color.of(128, 0, 0, alpha.get()).toBuffer(); //DARK_RED
    else if (itemFrames.getAsBoolean() && entity instanceof EntityItemFrame
        && ((EntityItemFrame) entity).getDisplayedItem().getItem() instanceof ItemShulkerBox)
      return Color.of(255, 191, 0, alpha.get()).toBuffer(); //GOLD
    else if (itemFrames.getAsBoolean() && entity instanceof EntityItemFrame
        && !(((EntityItemFrame) entity).getDisplayedItem().getItem() instanceof ItemShulkerBox))
      return Color.of(153, 102, 51, alpha.get()).toBuffer(); //BROWN

    else return -1;
  }

  @SubscribeEvent(priority = EventPriority.HIGH)
  public void onRender(final RenderEvent event) {
    if(MC.gameSettings.hideGUI || getWorld() == null) {
      return;
    }

    if (antialias.get()) {
      GL11.glEnable(GL11.GL_POLYGON_SMOOTH);
      GL11.glEnable(GL11.GL_LINE_SMOOTH);
    }

    int buf = 0;
    for (TileEntity tileEntity : getWorld().loadedTileEntityList) {
      if (tileEntity instanceof TileEntityChest) buf++;
      BlockPos pos = tileEntity.getPos();

      int outlineColor = getTileEntityOutlineColor(tileEntity);
      int fillColor = getTileEntityFillColor(tileEntity);
      if (outlineColor != -1 && fillColor != -1) {
        if (outline.get()) {
          GlStateManager.glLineWidth(width.get());
          event.getBuffer().begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
          GeometryTessellator.drawCuboid(event.getBuffer(), pos, GeometryMasks.Line.ALL, outlineColor);
          event.getTessellator().draw();
        }

        if (fill.get()) {
          event.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
          GeometryTessellator.drawCuboid(event.getBuffer(), pos, GeometryMasks.Quad.ALL, fillColor);
          event.getTessellator().draw();
        }
      }
    }
    count = buf;

    for (Entity entity : getWorld().loadedEntityList) {
      BlockPos pos = entity.getPosition();
      int outlineColor = getEntityOutlineColor(entity);
      int fillColor = getEntityFillColor(entity);

      if (outlineColor != -1 && fillColor != -1) {
        if (outline.get()) {
          GlStateManager.glLineWidth(width.get());
          event.getBuffer().begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
          GeometryTessellator.drawCuboid(
              event.getBuffer(),
              entity instanceof EntityItemFrame ? pos.add(0, -1, 0) : pos,
              GeometryMasks.Line.ALL,
              outlineColor);
          event.getTessellator().draw();
        }

        if (fill.get()) {
          event.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
          GeometryTessellator.drawCuboid(
              event.getBuffer(),
              entity instanceof EntityItemFrame ? pos.add(0, -1, 0) : pos,
              GeometryMasks.Quad.ALL,
              fillColor);
          event.getTessellator().draw();
        }
      }
    }
    if (antialias.get()) {
      GL11.glDisable(GL11.GL_POLYGON_SMOOTH);
      GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }
    GlStateManager.glLineWidth(1.0f);
  }
}
