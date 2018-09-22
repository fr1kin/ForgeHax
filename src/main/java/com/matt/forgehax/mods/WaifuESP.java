package com.matt.forgehax.mods;

import com.matt.forgehax.Helper;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.entity.LocalPlayerUtils;
import com.matt.forgehax.util.math.VectorUtils;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.function.Function;


@RegisterMod
public class WaifuESP extends ToggleMod {
    public WaifuESP() { super (Category.RENDER, "WaifuESP", false, "overlay cute animes over players"); }

    public final Setting<Boolean> noRenderPlayers = getCommandStub().builders().<Boolean>newSettingBuilder()
            .name("noRenderPlayers")
            .description("render other players")
            .defaultTo(false)
            .build();

    //private final ResourceLocation waifu = new ResourceLocation("textures/forgehax/waifu1.png");
    private ResourceLocation waifu;

    private final String waifuUrl = "https://raw.githubusercontent.com/fr1kin/ForgeHax/master/src/main/resources/assets/minecraft/textures/forgehax/waifu1.png";

    private final File waifuCache = new File(Helper.getFileManager().getCacheDirectory(), "waifu.png");

    private <T> BufferedImage getImage(T source, ThrowingFunction<T, BufferedImage> readFunction) {
        try {
            return readFunction.apply(source);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private boolean shouldDraw(EntityLivingBase entity) {
        return LocalPlayerUtils.isTargetEntity(entity) || (
                !entity.equals(MC.player) &&
                        EntityUtils.isAlive(entity) &&
                        EntityUtils.isValidEntity(entity) && (
                        EntityUtils.isPlayer(entity))
                );
    }



    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRenderGameOverlayEvent (RenderGameOverlayEvent.Text event) {
        if (waifu == null) return;

        for (Entity entity : MC.world.loadedEntityList) {
            if (EntityUtils.isLiving(entity) && shouldDraw((EntityLivingBase) entity)) {
                EntityLivingBase living = (EntityLivingBase) (entity);
                Vec3d bottomVec = EntityUtils.getInterpolatedPos(living, event.getPartialTicks());
                Vec3d topVec = bottomVec.add(new Vec3d(0, (entity.getRenderBoundingBox().maxY - entity.posY), 0));
                VectorUtils.ScreenPos top = VectorUtils._toScreen(topVec.x, topVec.y, topVec.z);
                VectorUtils.ScreenPos bot = VectorUtils._toScreen(bottomVec.x, bottomVec.y, bottomVec.z);
                if (top.isVisible || bot.isVisible) {

                    int height = (bot.y - top.y);
                    int width = height;

                    int x = (int)(top.x - (width / 1.8)); // normally 2.0 but lowering it shifts it to the left
                    int y = top.y;

                    // draw waifu
                    MC.renderEngine.bindTexture(waifu);

                    GlStateManager.color(255,255,255);
                    Gui.drawScaledCustomSizeModalRect(x, y, 0, 0, width, height, width, height, width, height);
                }
            }
        }
    }


    @SubscribeEvent
    public void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if(noRenderPlayers.getAsBoolean() && !event.getEntity().equals(MC.player)) {
            event.setCanceled(true);
        }
    }

    @Override
    public void onLoad() {
        MC.addScheduledTask(() -> {
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
                if (image == null) { LOGGER.warn("Failed to download waifu image"); return; }

                DynamicTexture dynamicTexture = new DynamicTexture(image);
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
