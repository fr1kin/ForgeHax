package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getWorld;

import com.matt.forgehax.events.Render2DEvent;
import com.matt.forgehax.util.color.Colors;
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
        .forEach(
            entity -> {
              Vec3d bottomPos = EntityUtils.getInterpolatedPos(entity, event.getPartialTicks());
              Vec3d topPos =
                  bottomPos.addVector(0.D, entity.getRenderBoundingBox().maxY - entity.posY, 0.D);

              Plane top = VectorUtils.toScreen(topPos);
              Plane bot = VectorUtils.toScreen(bottomPos);

              if (!top.isVisible() && !bot.isVisible()) return;

              double offX = bot.getX() - top.getX();
              double offY = bot.getY() - top.getY();

              GlStateManager.pushMatrix();
              GlStateManager.translate(top.getX() - (offX / 2.D), bot.getY(), 0);

              ItemStack stack = entity.getItem();
              String text =
                  stack.getDisplayName() + (stack.isStackable() ? (" x" + stack.getCount()) : "");

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
}
