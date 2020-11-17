package dev.fiki.forgehax.main.mods.render;

import dev.fiki.forgehax.api.cmd.settings.FloatSetting;
import dev.fiki.forgehax.api.mod.ToggleMod;

// TODO: 1.15 fix this
//@RegisterMod(
//    name = "SchematicHelper",
//    description = "Render box where a block would be placed",
//    category = Category.RENDER
//)
public class SchematicHelper extends ToggleMod {
  
  private final FloatSetting line_width = newFloatSetting()
          .name("width")
          .description("Line width")
          .defaultTo(5f)
          .min(1f)
          .build();
  
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
