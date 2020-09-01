package dev.fiki.forgehax.main.mods.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.main.util.draw.SurfaceHelper;
import dev.fiki.forgehax.main.util.entity.EntityUtils;
import dev.fiki.forgehax.main.util.math.ScreenPos;
import dev.fiki.forgehax.main.util.math.VectorUtils;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

//@RegisterMod(
//    name = "WaifuESP",
//    description = "overlay cute animes over players",
//    category = Category.RENDER
//)
public class WaifuESP extends ToggleMod {

  public final BooleanSetting noRenderPlayers = newBooleanSetting()
      .name("no-render-players")
      .description("render other players")
      .defaultTo(false)
      .build();

  // private final ResourceLocation waifu = new ResourceLocation("textures/forgehax/waifu1.png");
  private ResourceLocation waifu;

  private final String waifuUrl = "https://raw.githubusercontent.com/forgehax/assets/master/img/waifu_v01.png";

  private final File waifuCache =
      Common.getFileManager().getBaseResolve("cache/waifu.png").toFile();

  private <T> BufferedImage getImage(T source, ThrowingFunction<T, BufferedImage> readFunction) {
    try {
      return readFunction.apply(source);
    } catch (IOException ex) {
      ex.printStackTrace();
      return null;
    }
  }

  private boolean shouldDraw(LivingEntity entity) {
    return (!entity.equals(Common.getLocalPlayer())
        && EntityUtils.isAlive(entity)
        && EntityUtils.isValidEntity(entity)
        && (EntityUtils.isPlayer(entity)));
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void onRenderGameOverlayEvent(RenderGameOverlayEvent.Text event) {
    if (waifu == null) {
      return;
    }

    for (Entity entity : Common.getWorld().getAllEntities()) {
      if (EntityUtils.isLiving(entity) && shouldDraw((LivingEntity) entity)) {
        LivingEntity living = (LivingEntity) (entity);
        Vector3d bottomVec = EntityUtils.getInterpolatedPos(living, event.getPartialTicks());
        Vector3d topVec =
            bottomVec.add(new Vector3d(0, (entity.getRenderBoundingBox().maxY - entity.getPosY()), 0));
        ScreenPos top = VectorUtils.toScreen(topVec.x, topVec.y, topVec.z);
        ScreenPos bot = VectorUtils.toScreen(bottomVec.x, bottomVec.y, bottomVec.z);
        if (top.isVisible() || bot.isVisible()) {

          int height = (bot.getYAsInteger() - top.getYAsInteger());
          int width = height;

          int x = (int) (top.getXAsInteger() - (width / 1.8)); // normally 2.0 but lowering it shifts it to the left
          int y = top.getYAsInteger();

          // draw waifu
          Common.MC.getTextureManager().bindTexture(waifu);

          RenderSystem.color4f(1.f, 1.f, 1.f, 1.f);
          SurfaceHelper.drawScaledCustomSizeModalRect(
              x, y, 0, 0, width, height, width, height, width, height);
        }
      }
    }
  }

  @SubscribeEvent
  public void onRenderPlayer(RenderPlayerEvent.Pre event) {
    if (noRenderPlayers.getValue() && !event.getEntity().equals(Common.MC.player)) {
      event.setCanceled(true);
    }
  }

  @Override
  public void onLoad() {
    Common.addScheduledTask(
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
              Common.getLogger().warn("Failed to download waifu image");
              return;
            }

            // TODO: 1.15 BufferedImage -> NativeImage
            DynamicTexture dynamicTexture = new DynamicTexture(null);
            dynamicTexture.loadTexture(Common.MC.getResourceManager());
            waifu = Common.MC.getTextureManager().getDynamicTextureLocation("WAIFU", dynamicTexture);
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
