package com.matt.forgehax.mods;

import com.matt.forgehax.Helper;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.math.VectorUtils;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.system.MemoryUtil;

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

    // default to missing texture, will be scheduled to be set in onLoad()
    private ResourceLocation waifu = MissingTextureSprite.getLocation();

    private final String waifuUrl =
        "https://raw.githubusercontent.com/fr1kin/ForgeHax/master/src/main/resources/assets/minecraft/textures/forgehax/waifu1.png";

    private final Path waifuCache = Helper.getFileManager().getBaseResolve("cache/waifu.png");

    private boolean shouldDraw(LivingEntity entity) {
        return (!entity.equals(MC.player)
            && EntityUtils.isAlive(entity)
            && EntityUtils.isValidEntity(entity)
            && (EntityUtils.isPlayer(entity)));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRenderGameOverlayEvent(RenderGameOverlayEvent.Text event) {
        if (waifu == null) return;

        for (Entity entity : MC.world.func_217416_b()) {
            if (EntityUtils.isLiving(entity) && shouldDraw((LivingEntity) entity)) {
                LivingEntity living = (LivingEntity) (entity);
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

                    // draw waifu
                    MC.textureManager.bindTexture(waifu);

                    GlStateManager.color3f(255, 255, 255);
                    AbstractGui.blit( // 1.14: drawScaledCustomSizeModalRect renamed to blit
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
    public void onLoad() { // lol
        MC.execute(() -> {
            this.waifu = Optional.of(waifuCache)
                .map(cache -> {
                    try {
                        return Files.exists(cache) ?
                            new BufferedInputStream(Files.newInputStream(cache)) :
                            new URL(waifuUrl).openStream();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        return null;
                    }
                })
                .map(stream -> {
                    ByteBuffer byteBuffer = null;
                    try {
                        byteBuffer = TextureUtil.readResource(stream);
                        if (!Files.exists(waifuCache)) {
                            byteBuffer.rewind();
                            saveWaifuToCache(byteBuffer);
                        }
                        byteBuffer.rewind();
                        return NativeImage.read(byteBuffer);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        return null;
                    } finally {
                        if (byteBuffer != null)
                            MemoryUtil.memFree(byteBuffer);
                    }
                })
                .map(image -> {
                    final DynamicTexture dynamicTexture = new DynamicTexture(image);
                    final ResourceLocation waifuLocation = new ResourceLocation("forgehax_waifu");
                    MC.getTextureManager().loadTexture(waifuLocation, dynamicTexture);
                    return waifuLocation;
                })
                .orElse(MissingTextureSprite.getLocation());
        });
    }

    private void saveWaifuToCache(ByteBuffer buffer) {
        try {
            FileChannel fileOut = new FileOutputStream(waifuCache.toFile()).getChannel();
            fileOut.write(buffer);
            fileOut.close();
        } catch (IOException ex) {
            LOGGER.warn("Failed to save waifu to cache: " + ex);
            ex.printStackTrace();
        }
    }


}
