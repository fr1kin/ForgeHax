package dev.fiki.forgehax.main.mods.render;

import dev.fiki.forgehax.api.cmd.settings.ColorSetting;
import dev.fiki.forgehax.api.cmd.settings.FloatSetting;
import dev.fiki.forgehax.api.color.Colors;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;

@RegisterMod(
    name = "BlockHighlight",
    description = "Make selected block bounding box more visible",
    category = Category.RENDER
)
public class BlockHighlightMod extends ToggleMod {

  private final ColorSetting color = newColorSetting()
      .name("color")
      .description("Block highlight color")
      .defaultTo(Colors.RED)
      .build();

  private final FloatSetting width = newFloatSetting()
      .name("width")
      .description("line width")
      .min(0.f)
      .defaultTo(5.f)
      .build();

  private float toFloat(int colorVal) {
    return colorVal / 255.f;
  }

//  public void onRenderBoxPre(DrawBlockBoundingBoxEvent.Pre event) {
//    GlStateManager.disableDepthTest();
//    GlStateManager.lineWidth(width.get());
//    event.alpha = toFloat(alpha.get());
//    event.red = toFloat(red.get());
//    event.green = toFloat(green.get());
//    event.blue = toFloat(blue.get());
//  }
//
//  public void onRenderBoxPost(DrawBlockBoundingBoxEvent.Post event) {
//    GlStateManager.enableDepthTest();
//  }
}
