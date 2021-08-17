package dev.fiki.forgehax.main.mods.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.fiki.forgehax.api.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.api.common.PriorityEnum;
import dev.fiki.forgehax.api.draw.SurfaceHelper;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.render.LivingRenderEvent;
import dev.fiki.forgehax.api.events.render.RenderPlaneEvent;
import dev.fiki.forgehax.api.extension.EntityEx;
import dev.fiki.forgehax.api.extension.VectorEx;
import dev.fiki.forgehax.api.math.ScreenPos;
import dev.fiki.forgehax.api.math.VectorUtil;
import dev.fiki.forgehax.api.mod.ToggleMod;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import static dev.fiki.forgehax.main.Common.*;

//@RegisterMod(
//    name = "WaifuESP",
//    description = "overlay cute animes over players",
//    category = Category.RENDER
//)
@ExtensionMethod({EntityEx.class, VectorEx.class})
public class WaifuESP extends ToggleMod {

  public final BooleanSetting noRenderPlayers = newBooleanSetting()
      .name("no-render-players")
      .description("render other players")
      .defaultTo(false)
      .build();

  // private final ResourceLocation waifu = new ResourceLocation("textures/forgehax/waifu1.png");
  private ResourceLocation waifu;

  private final String waifuUrl = "https://raw.githubusercontent.com/forgehax/assets/master/img/waifu_v01.png";

  private final File waifuCache = getFileManager().getBaseResolve("cache/waifu.png").toFile();

  private <T> BufferedImage getImage(T source, ThrowingFunction<T, BufferedImage> readFunction) {
    try {
      return readFunction.apply(source);
    } catch (IOException ex) {
      ex.printStackTrace();
      return null;
    }
  }

  private boolean shouldDraw(LivingEntity entity) {
    return entity.isPlayerType()
        && !entity.isLocalPlayer()
        && entity.isReallyAlive()
        && entity.isValidEntity();
  }

  @SubscribeListener(priority = PriorityEnum.LOWEST)
  public void onRenderGameOverlayEvent(RenderPlaneEvent.Back event) {
    if (waifu == null) {
      return;
    }

    for (Entity entity : getWorld().entitiesForRendering()) {
      if (entity.showVehicleHealth() && shouldDraw((LivingEntity) entity)) {
        LivingEntity living = (LivingEntity) (entity);
        Vector3d bottomVec = living.getInterpolatedPos(event.getPartialTicks());
        Vector3d topVec =
            bottomVec.add(new Vector3d(0, (entity.getBoundingBox().maxY - entity.getY()), 0));
        ScreenPos top = VectorUtil.toScreen(topVec);
        ScreenPos bot = VectorUtil.toScreen(bottomVec);
        if (top.isVisible() || bot.isVisible()) {

          int height = (bot.getYAsInteger() - top.getYAsInteger());
          int width = height;

          int x = (int) (top.getXAsInteger() - (width / 1.8)); // normally 2.0 but lowering it shifts it to the left
          int y = top.getYAsInteger();

          // draw waifu
          MC.getTextureManager().bind(waifu);

          RenderSystem.color4f(1.f, 1.f, 1.f, 1.f);
          SurfaceHelper.drawScaledCustomSizeModalRect(
              x, y, 0, 0, width, height, width, height, width, height);
        }
      }
    }
  }

  @SubscribeListener
  public void onRenderPlayer(LivingRenderEvent.Pre<?, ?> event) {
    if (noRenderPlayers.getValue()
        && event.getLiving().isPlayerType()
        && !event.getLiving().equals(getLocalPlayer())) {
      event.setCanceled(true);
    }
  }

  @Override
  public void onLoad() {
    addScheduledTask(
        () -> {
          try {
            BufferedImage image;
            if (waifuCache.exists()) { // TODO: download async
              image = getImage(waifuCache, ImageIO::read); // from cache
            } else {
              image = getImage(new URL(waifuUrl), ImageIO::read); // from internet
              if (image != null) {
                try {
                  ImageIO.write(image, "png", waifuCache);
                } catch (IOException ex) {
                  ex.printStackTrace();
                }
              }
            }
            if (image == null) {
              log.warn("Failed to download waifu image");
              return;
            }

            // TODO: 1.15 BufferedImage -> NativeImage
            DynamicTexture dynamicTexture = new DynamicTexture(null);
            dynamicTexture.load(MC.getResourceManager());
            waifu = MC.getTextureManager().register("WAIFU", dynamicTexture);
          } catch (Exception e) {
            e.printStackTrace();
          }
        });
  }

  @FunctionalInterface
  private interface ThrowingFunction<T, R> {
    R apply(T obj) throws IOException;
  }
}
