package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.util.command.Setting;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod
public class BlockHighlightMod extends ToggleMod {
  
  private final Setting<Integer> alpha =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("alpha")
          .description("alpha")
          .min(0)
          .max(255)
          .defaultTo(255)
          .build();
  private final Setting<Integer> red =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("red")
          .description("red")
          .min(0)
          .max(255)
          .defaultTo(0)
          .build();
  private final Setting<Integer> green =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("green")
          .description("green")
          .min(0)
          .max(255)
          .defaultTo(0)
          .build();
  private final Setting<Integer> blue =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("blue")
          .description("blue")
          .min(0)
          .max(255)
          .defaultTo(0)
          .build();
  
  private final Setting<Float> width =
      getCommandStub()
          .builders()
          .<Float>newSettingBuilder()
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
