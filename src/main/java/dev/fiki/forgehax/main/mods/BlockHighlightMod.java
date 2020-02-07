package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.util.cmd.settings.ColorSetting;
import dev.fiki.forgehax.main.util.cmd.settings.FloatSetting;
import dev.fiki.forgehax.main.util.color.Colors;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod
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

  public BlockHighlightMod() {
    super(Category.RENDER, "BlockHighlight", false, "Make selected block bounding box more visible");
  }

  private float toFloat(int colorVal) {
    return colorVal / 255.f;
  }

//  @SubscribeEvent
//  public void onRenderBoxPre(DrawBlockBoundingBoxEvent.Pre event) {
//    GlStateManager.disableDepthTest();
//    GlStateManager.lineWidth(width.get());
//    event.alpha = toFloat(alpha.get());
//    event.red = toFloat(red.get());
//    event.green = toFloat(green.get());
//    event.blue = toFloat(blue.get());
//  }
//
//  @SubscribeEvent
//  public void onRenderBoxPost(DrawBlockBoundingBoxEvent.Post event) {
//    GlStateManager.enableDepthTest();
//  }
}
