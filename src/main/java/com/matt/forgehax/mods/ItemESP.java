package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getWorld;

import com.matt.forgehax.events.Render2DEvent;
import com.matt.forgehax.events.RenderEvent;
import com.matt.forgehax.util.draw.RenderUtils;
import com.matt.forgehax.util.color.Colors;
import com.matt.forgehax.util.color.Color;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.draw.SurfaceHelper;
import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.math.Plane;
import com.matt.forgehax.util.math.VectorUtils;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class ItemESP extends ToggleMod {
  
  public ItemESP() {
    super(Category.RENDER, "ItemESP", false, "ESP for items");
  }
  
  public final Setting<Double> scale =
      getCommandStub()
          .builders()
          .<Double>newSettingBuilder()
          .name("scale")
          .description("Scaling for text")
          .defaultTo(1.D)
          .min(0.D)
          .build();

  public final Setting<Boolean> age =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("age")
          .description("Show how long the item has existed (clientside)")
          .defaultTo(false)
          .build();

  public final Setting<Float> linewidth =
    getCommandStub()
      .builders()
      .<Float>newSettingBuilder()
      .name("width")
      .description("Line width")
      .defaultTo(2.0F)
      .build();

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

  private final Setting<Boolean> draw_box =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("box")
          .description("Draw a box around items")
          .defaultTo(false)
          .build();

  // private final int MAX_AGE = 6000; not needed! Awesome!
  private final int TICKS_SECOND = 20;
  
  @SubscribeEvent
  public void onRender2D(final Render2DEvent event) {
    GlStateManager.enableBlend();
    GlStateManager.tryBlendFuncSeparate(
        GlStateManager.SourceFactor.SRC_ALPHA,
        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
        GlStateManager.SourceFactor.ONE,
        GlStateManager.DestFactor.ZERO);
    GlStateManager.enableTexture2D();
    GlStateManager.disableDepth();
    
    final double scale = this.scale.get() == 0 ? 1.D : this.scale.get();
    
    getWorld()
        .loadedEntityList
        .stream()
        .filter(EntityItem.class::isInstance)
        .map(EntityItem.class::cast)
        .filter(entity -> entity.ticksExisted > 1)
        .forEach(
            entity -> {
              Vec3d bottomPos = EntityUtils.getInterpolatedPos(entity, event.getPartialTicks());
              Vec3d topPos =
                  bottomPos.addVector(0.D, entity.getRenderBoundingBox().maxY - entity.posY, 0.D);
              
              Plane top = VectorUtils.toScreen(topPos);
              Plane bot = VectorUtils.toScreen(bottomPos);
              
              if (!top.isVisible() && !bot.isVisible()) {
                return;
              }
              
              double offX = bot.getX() - top.getX();
              double offY = bot.getY() - top.getY();
              
              GlStateManager.pushMatrix();
              GlStateManager.translate(top.getX() - (offX / 2.D), bot.getY(), 0);
              
              ItemStack stack = entity.getItem();
              String text =
                  stack.getDisplayName() + (stack.isStackable() ? (" x" + stack.getCount()) : "");

              if (age.get()) {
                text += String.format(" [%d]", entity.getAge()/TICKS_SECOND);
              }
              
              SurfaceHelper.drawTextShadow(
                  text,
                  (int) (offX / 2.D - SurfaceHelper.getTextWidth(text, scale) / 2.D),
                  -(int) (offY - SurfaceHelper.getTextHeight(scale) / 2.D) - 1,
                  Colors.WHITE.toBuffer(),
                  scale);
              
              GlStateManager.popMatrix();
            });
    
    GlStateManager.enableDepth();
    GlStateManager.disableBlend();
  }

  @SubscribeEvent
  public void onRender(final RenderEvent event) {
    if (!draw_box.get()) return;
    getWorld()
        .loadedEntityList
        .stream()
        .filter(EntityItem.class::isInstance)
        .map(EntityItem.class::cast)
        .filter(entity -> entity.ticksExisted > 1)
        .forEach(
            entity -> {
	  	        int color = Color.of(red.get(), green.get(), blue.get(), alpha.get()).toBuffer();
              AxisAlignedBB bb = entity.getRenderBoundingBox();
              Vec3d minVec = new Vec3d(bb.minX, bb.minY, bb.minZ);
              Vec3d maxVec = new Vec3d(bb.maxX, bb.maxY, bb.maxZ);

              // GlStateManager.enableDepth();
              // GlStateManager.glLineWidth(linewidth.get());
              // GL11.glEnable(GL11.GL_LINE_SMOOTH);

              RenderUtils.drawBox(minVec, maxVec, color, linewidth.get(), true);

              // GL11.glDisable(GL11.GL_LINE_SMOOTH);
              // GlStateManager.glLineWidth(1.0f);
              // GlStateManager.disableDepth();
            });
  }

}
