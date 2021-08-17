package dev.fiki.forgehax.main.mods.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.fiki.forgehax.api.cmd.settings.ColorSetting;
import dev.fiki.forgehax.api.cmd.settings.FloatSetting;
import dev.fiki.forgehax.api.color.Colors;
import dev.fiki.forgehax.api.draw.SurfaceHelper;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.render.RenderPlaneEvent;
import dev.fiki.forgehax.api.extension.EntityEx;
import dev.fiki.forgehax.api.math.ScreenPos;
import dev.fiki.forgehax.api.math.VectorUtil;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3d;

import static dev.fiki.forgehax.main.Common.getBufferProvider;
import static dev.fiki.forgehax.main.Common.worldEntities;

@RegisterMod(
    name = "ItemESP",
    description = "ESP for items",
    category = Category.RENDER
)
@ExtensionMethod({EntityEx.class})
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

  @SubscribeListener
  public void onRender2D(final RenderPlaneEvent.Back event) {
    final float scale = this.scale.getValue() == 0 ? 1.f : this.scale.getValue();

    final IRenderTypeBuffer.Impl buffers = getBufferProvider().getBufferSource();
    final MatrixStack stack = new MatrixStack();

    worldEntities()
        .filter(ItemEntity.class::isInstance)
        .map(ItemEntity.class::cast)
        .filter(entity -> entity.tickCount > 1)
        .forEach(entity -> {
          Vector3d bottomPos = entity.getInterpolatedPos(event.getPartialTicks());
          Vector3d topPos = bottomPos.add(0.D, entity.getBoundingBox().maxY - entity.getY(), 0.D);

          ScreenPos top = VectorUtil.toScreen(topPos);
          ScreenPos bot = VectorUtil.toScreen(bottomPos);

          if (!top.isVisible() && !bot.isVisible()) {
            return;
          }

          stack.pushPose();
          stack.translate(top.getX(), bot.getY(), 0.f);

          ItemStack itemStack = entity.getItem();
          String text = itemStack.getDisplayName().getString()
              + (itemStack.isStackable() ? (" x" + itemStack.getCount()) : "");

          stack.scale(scale, scale, 0.f);
          stack.translate(
              -SurfaceHelper.getStringWidth(text) / 2.f,
              -SurfaceHelper.getStringHeight() / 2.f,
              0.f);

          SurfaceHelper.renderString(buffers, stack.last().pose(),
              text, 0, 0, color.getValue(), true);

          stack.popPose();
        });

    buffers.endBatch();
  }
}
