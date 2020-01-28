package com.matt.forgehax.mods;

import com.matt.forgehax.Globals;
import com.matt.forgehax.Helper;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.draw.SurfaceHelper;
import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.math.VectorUtils;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.matt.forgehax.Globals.*;

@RegisterMod
public class WaifuESP extends ToggleMod {
  
  public WaifuESP() {
    super(Category.RENDER, "WaifuESP", false, "overlay cute animes over players");
  }
  
  public final Setting<Boolean> noRenderPlayers =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("noRenderPlayers")
          .description("render other players")
          .defaultTo(false)
          .build();
  
  // private final ResourceLocation waifu = new ResourceLocation("textures/forgehax/waifu1.png");
  private ResourceLocation waifu;
  
  private final String waifuUrl = "https://raw.githubusercontent.com/forgehax/assets/master/img/waifu_v01.png";
  
  private final File waifuCache =
      Helper.getFileManager().getBaseResolve("cache/waifu.png").toFile();
  
  private <T> BufferedImage getImage(T source, ThrowingFunction<T, BufferedImage> readFunction) {
    try {
      return readFunction.apply(source);
    } catch (IOException ex) {
      ex.printStackTrace();
      return null;
    }
  }
  
  private boolean shouldDraw(LivingEntity entity) {
    return (!entity.equals(getLocalPlayer())
        && EntityUtils.isAlive(entity)
        && EntityUtils.isValidEntity(entity)
        && (EntityUtils.isPlayer(entity)));
  }
  
  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void onRenderGameOverlayEvent(RenderGameOverlayEvent.Text event) {
    if (waifu == null) {
      return;
    }
    
    for (Entity entity : getWorld().getAllEntities()) {
      if (EntityUtils.isLiving(entity) && shouldDraw((LivingEntity) entity)) {
        LivingEntity living = (LivingEntity) (entity);
        Vec3d bottomVec = EntityUtils.getInterpolatedPos(living, event.getPartialTicks());
        Vec3d topVec =
            bottomVec.add(new Vec3d(0, (entity.getRenderBoundingBox().maxY - entity.getPosY()), 0));
        VectorUtils.ScreenPos top = VectorUtils._toScreen(topVec.x, topVec.y, topVec.z);
        VectorUtils.ScreenPos bot = VectorUtils._toScreen(bottomVec.x, bottomVec.y, bottomVec.z);
        if (top.isVisible || bot.isVisible) {
          
          int height = (bot.y - top.y);
          int width = height;
          
          int x =
              (int) (top.x - (width / 1.8)); // normally 2.0 but lowering it shifts it to the left
          int y = top.y;
          
          // draw waifu
          MC.getTextureManager().bindTexture(waifu);
          
          RenderSystem.color4f(1.f, 1.f, 1.f, 1.f);
          SurfaceHelper.drawScaledCustomSizeModalRect(
              x, y, 0, 0, width, height, width, height, width, height);
        }
      }
    }
  }
  
  @SubscribeEvent
  public void onRenderPlayer(RenderPlayerEvent.Pre event) {
    if (noRenderPlayers.getAsBoolean() && !event.getEntity().equals(MC.player)) {
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
              LOGGER.warn("Failed to download waifu image");
              return;
            }

            // TODO: 1.15 BufferedImage -> NativeImage
            DynamicTexture dynamicTexture = new DynamicTexture(null);
            dynamicTexture.loadTexture(MC.getResourceManager());
            waifu = MC.getTextureManager().getDynamicTextureLocation("WAIFU", dynamicTexture);
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
