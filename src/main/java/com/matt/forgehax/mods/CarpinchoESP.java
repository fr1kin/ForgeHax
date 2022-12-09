package com.matt.forgehax.mods;

import com.matt.forgehax.Helper;
import com.matt.forgehax.util.command.Setting;
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
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class CarpinchoESP extends ToggleMod {
  
  public CarpinchoESP() {
    super(Category.RENDER, "CarpinchoESP", false, "overlay cute capybaras over players");
  }
  
  public final Setting<Boolean> noRenderPlayers =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("noRenderPlayers")
          .description("render other players")
          .defaultTo(false)
          .build();
  
  // private final ResourceLocation carpincho = new ResourceLocation("textures/forgehax/carpincho.png");
  private ResourceLocation carpincho;
  
  private final String carpinchoUrl = "https://cdn.discordapp.com/attachments/932122296863322172/1050581091259531284/carpincho.png";
  
  private final File carpinchoCache =
      Helper.getFileManager().getBaseResolve("cache/carpincho.png").toFile();
  
  private <T> BufferedImage getImage(T source, ThrowingFunction<T, BufferedImage> readFunction) {
    try {
      return readFunction.apply(source);
    } catch (IOException ex) {
      ex.printStackTrace();
      return null;
    }
  }
  
  private boolean shouldDraw(EntityLivingBase entity) {
    return (!entity.equals(MC.player)
        && EntityUtils.isAlive(entity)
        && EntityUtils.isValidEntity(entity)
        && (EntityUtils.isPlayer(entity)));
  }
  
  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void onRenderGameOverlayEvent(RenderGameOverlayEvent.Text event) {
    if (carpincho == null) {
      return;
    }
    
    for (Entity entity : MC.world.loadedEntityList) {
      if (EntityUtils.isLiving(entity) && shouldDraw((EntityLivingBase) entity)) {
        EntityLivingBase living = (EntityLivingBase) (entity);
        Vec3d bottomVec = EntityUtils.getInterpolatedPos(living, event.getPartialTicks());
        Vec3d topVec =
            bottomVec.add(new Vec3d(0, (entity.getRenderBoundingBox().maxY - entity.posY), 0));
        VectorUtils.ScreenPos top = VectorUtils._toScreen(topVec.x, topVec.y, topVec.z);
        VectorUtils.ScreenPos bot = VectorUtils._toScreen(bottomVec.x, bottomVec.y, bottomVec.z);
        if (top.isVisible || bot.isVisible) {
          
          int height = (bot.y - top.y);
          int width = height;
          
          int x =
              (int) (top.x - (width / 1.8)); // normally 2.0 but lowering it shifts it to the left
          int y = top.y;
          
          // draw carpincho
          MC.renderEngine.bindTexture(carpincho);
          
          GlStateManager.color(255, 255, 255);
          Gui.drawScaledCustomSizeModalRect(
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
    MC.addScheduledTask(
        () -> {
          try {
            BufferedImage image;
            if (carpinchoCache.exists()) { // TODO: download async
              image = getImage(carpinchoCache, ImageIO::read); // from cache
            } else {
              image = getImage(new URL(carpinchoUrl), ImageIO::read); // from internet
              if (image != null) {
                try {
                  ImageIO.write(image, "png", carpinchoCache);
                } catch (IOException ex) {
                  ex.printStackTrace();
                }
              }
            }
            if (image == null) {
              LOGGER.warn("Failed to download carpincho image");
              return;
            }
            
            DynamicTexture dynamicTexture = new DynamicTexture(image);
            dynamicTexture.loadTexture(MC.getResourceManager());
            carpincho = MC.getTextureManager().getDynamicTextureLocation("CARPINCHO", dynamicTexture);
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
