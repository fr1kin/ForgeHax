package dev.fiki.forgehax.main.mods;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.events.Render2DEvent;
import dev.fiki.forgehax.main.util.cmd.settings.ColorSetting;
import dev.fiki.forgehax.main.util.cmd.settings.DoubleSetting;
import dev.fiki.forgehax.main.util.cmd.settings.FloatSetting;
import dev.fiki.forgehax.main.util.color.Colors;
import dev.fiki.forgehax.main.util.draw.SurfaceHelper;
import dev.fiki.forgehax.main.util.entity.EntityUtils;
import dev.fiki.forgehax.main.util.math.Plane;
import dev.fiki.forgehax.main.util.math.VectorUtils;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.stream.StreamSupport;

import static dev.fiki.forgehax.main.Common.getBufferProvider;
import static dev.fiki.forgehax.main.Common.worldEntities;

@RegisterMod
public class ItemESP extends ToggleMod {

  private final FloatSetting scale = newFloatSetting()
      .name("scale")
      .description("Scaling for text")
      .defaultTo(1.f)
      .min(0.f)
      .build();

  private final ColorSetting color = newColorSetting()
      .name("color")
      .description("Color of the item esp font")
      .defaultTo(Colors.WHITE)
      .build();

  public ItemESP() {
    super(Category.RENDER, "ItemESP", false, "ESP for items");
  }

  @SubscribeEvent
  public void onRender2D(final Render2DEvent event) {
    final float scale = this.scale.getValue() == 0 ? 1.f : this.scale.getValue();

    final IRenderTypeBuffer.Impl buffers = getBufferProvider().getBufferSource();
    final MatrixStack stack = new MatrixStack();

    worldEntities()
        .filter(ItemEntity.class::isInstance)
        .map(ItemEntity.class::cast)
        .filter(entity -> entity.ticksExisted > 1)
        .forEach(entity -> {
          Vec3d bottomPos = EntityUtils.getInterpolatedPos(entity, event.getPartialTicks());
          Vec3d topPos = bottomPos.add(0.D, entity.getRenderBoundingBox().maxY - entity.getPosY(), 0.D);

          Plane top = VectorUtils.toScreen(topPos);
          Plane bot = VectorUtils.toScreen(bottomPos);

          if (!top.isVisible() && !bot.isVisible()) {
            return;
          }

          stack.push();
          stack.translate(top.getX(), bot.getY(), 0.f);

          ItemStack itemStack = entity.getItem();
          String text = itemStack.getDisplayName().getUnformattedComponentText()
              + (itemStack.isStackable() ? (" x" + itemStack.getCount()) : "");

          stack.scale(scale, scale, 0.f);
          stack.translate(
              -SurfaceHelper.getStringWidth(text) / 2.f,
              -SurfaceHelper.getStringHeight() / 2.f,
              0.f);

          SurfaceHelper.renderString(buffers, stack.getLast().getPositionMatrix(),
              text, 0, 0, color.getValue(), true);

          stack.pop();
        });

    buffers.finish();
  }
}
