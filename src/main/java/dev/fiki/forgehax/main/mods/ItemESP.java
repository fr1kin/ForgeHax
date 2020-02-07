package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.events.Render2DEvent;
import dev.fiki.forgehax.main.util.cmd.settings.DoubleSetting;
import dev.fiki.forgehax.main.util.color.Colors;
import dev.fiki.forgehax.main.util.draw.SurfaceHelper;
import dev.fiki.forgehax.main.util.entity.EntityUtils;
import dev.fiki.forgehax.main.util.math.Plane;
import dev.fiki.forgehax.main.util.math.VectorUtils;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.stream.StreamSupport;

@RegisterMod
public class ItemESP extends ToggleMod {

  public ItemESP() {
    super(Category.RENDER, "ItemESP", false, "ESP for items");
  }

  public final DoubleSetting scale = newDoubleSetting()
      .name("scale")
      .description("Scaling for text")
      .defaultTo(1.D)
      .min(0.D)
      .build();

  @SubscribeEvent
  public void onRender2D(final Render2DEvent event) {
    GlStateManager.enableBlend();
    GlStateManager.blendFuncSeparate(
        GlStateManager.SourceFactor.SRC_ALPHA.param,
        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA.param,
        GlStateManager.SourceFactor.ONE.param,
        GlStateManager.DestFactor.ZERO.param);
    GlStateManager.enableTexture();
    GlStateManager.disableDepthTest();

    final double scale = this.scale.getValue() == 0 ? 1.D : this.scale.getValue();

    StreamSupport.stream(Common.getWorld().getAllEntities().spliterator(), false)
        .filter(ItemEntity.class::isInstance)
        .map(ItemEntity.class::cast)
        .filter(entity -> entity.ticksExisted > 1)
        .forEach(
            entity -> {
              Vec3d bottomPos = EntityUtils.getInterpolatedPos(entity, event.getPartialTicks());
              Vec3d topPos =
                  bottomPos.add(0.D, entity.getRenderBoundingBox().maxY - entity.getPosY(), 0.D);

              Plane top = VectorUtils.toScreen(topPos);
              Plane bot = VectorUtils.toScreen(bottomPos);

              if (!top.isVisible() && !bot.isVisible()) {
                return;
              }

              double offX = bot.getX() - top.getX();
              double offY = bot.getY() - top.getY();

              GlStateManager.pushMatrix();
              GlStateManager.translated(top.getX() - (offX / 2.D), bot.getY(), 0);

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

    GlStateManager.enableDepthTest();
    GlStateManager.disableBlend();
  }
}
