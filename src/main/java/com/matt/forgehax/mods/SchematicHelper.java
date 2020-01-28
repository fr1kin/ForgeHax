package com.matt.forgehax.mods;

import com.matt.forgehax.Globals;
import com.matt.forgehax.events.RenderEvent;
import com.matt.forgehax.util.color.Colors;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.matt.forgehax.util.schematica.Schematic;
import com.matt.forgehax.util.schematica.SchematicaHelper;
import com.matt.forgehax.util.tesselation.GeometryMasks;
import com.matt.forgehax.util.tesselation.GeometryTessellator;
import java.util.Optional;
import java.util.OptionalInt;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import static com.matt.forgehax.Globals.*;

// TODO: 1.15 fix this
public class SchematicHelper extends ToggleMod {
  
  private final Setting<Float> line_width =
      getCommandStub()
          .builders()
          .<Float>newSettingBuilder()
          .name("width")
          .description("Line width")
          .defaultTo(5f)
          .min(1f)
          .build();
  
  public SchematicHelper() {
    super(Category.RENDER, "SchematicHelper", false, "Render box where a block would be placed");
  }

  /*
  @SubscribeEvent
  public void onRender(RenderEvent event) {
    if (!SchematicaHelper.isSchematicaPresent()) return;

    final ItemStack heldStack = getLocalPlayer().getHeldItemMainhand();
    if (heldStack.isEmpty()) {
      return;
    }
    if (!(heldStack.getItem() instanceof BlockItem)) {
      return;
    }
    
    final Block block = ((BlockItem) heldStack.getItem()).getBlock();
    
    getBlockPlacePos().ifPresent(pos ->
        getColor(pos, heldBlockState).ifPresent(color -> {
          event.getBuffer().begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
          GlStateManager.lineWidth(line_width.get());
          
          GeometryTessellator.drawCuboid(event.getBuffer(), pos, GeometryMasks.Line.ALL, color);
          
          event.getTessellator().draw();
          
        })
    );
  }
  
  private static OptionalInt getColor(BlockPos pos, BlockState heldBlock) {
    final Optional<Tuple<Schematic, BlockPos>> optSchematic = SchematicaHelper.getOpenSchematic();
    if (optSchematic.isPresent()) {
      final Schematic schematic = optSchematic.get().getFirst();
      final BlockPos schematicOffset = optSchematic.get().getSecond();
      final BlockPos schematicPos = pos.subtract(schematicOffset);
      
      if (schematic.inSchematic(schematicPos)) {
        final IBlockState schematicBlock = schematic.desiredState(schematicPos);
        
        return OptionalInt
            .of(schematicBlock.equals(heldBlock) ? Colors.GREEN.toBuffer() : Colors.RED.toBuffer());
      } else {
        return OptionalInt.empty();
      }
    } else {
      return OptionalInt.empty();
    }
  }
  
  private static Optional<BlockPos> getBlockPlacePos() {
    return Optional.ofNullable(MC.objectMouseOver)
        .filter(result -> result.typeOfHit == RayTraceResult.Type.BLOCK)
        .map(ray -> ray.getBlockPos().offset(ray.sideHit));
  }*/
}
