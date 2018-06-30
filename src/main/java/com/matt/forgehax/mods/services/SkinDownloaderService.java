package com.matt.forgehax.mods.services;

import com.matt.forgehax.asm.events.PlayerListAddEvent;
import com.matt.forgehax.asm.events.SkinAvailableEvent;
import com.matt.forgehax.asm.events.SkinDownloadEvent;
import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.mod.ServiceMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.item.ItemFishingRod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Created by Babbaj on 2/16/2018.
 */
@RegisterMod
public class SkinDownloaderService extends ServiceMod {

    public SkinDownloaderService() {
        super("SkinDownloaderService", "Load skins in the background so tab list doesn't lag");
    }

    private final Set<ITextureObject> scheduledTextures = new HashSet<>();

    private static final ExecutorService EXECUTOR = new ThreadPoolExecutor(0, 1, 1L, TimeUnit.MINUTES, new LinkedBlockingQueue<>());

    // TODO: add hook to NetHandlerPlayClient::handlePlayerListItem
    //@SubscribeEvent
    public void onUpdate(LocalPlayerUpdateEvent event) {
        MC.getConnection().getPlayerInfoMap().stream()
                .map(NetworkPlayerInfo::getLocationSkin)
                .map(MC.getTextureManager()::getTexture)
                .forEach(tex -> {});
                /*.filter(ThreadDownloadImageData.class::isInstance)
                .map(tex -> (ThreadDownloadImageData)tex)
                .filter(tex -> !scheduledTextures.contains(tex))
                .filter(tex -> !FastReflection.Fields.ThreadDownloadImageData_textureUploaded.get(tex))
                .filter(tex -> FastReflection.Fields.ThreadDownloadImageData_bufferedImage.get(tex) != null)
                .forEach(this::schedule);*/


    }

    @SubscribeEvent
    public void onPlayerListAdd(PlayerListAddEvent event) {
        System.out.println("PlayerListAddEvent!");
        event.info.getLocationSkin(); // force skin to load
    }

    @SubscribeEvent
    public void onSkinAvailable(SkinAvailableEvent event) {
        //System.out.println("Skin available!!!");
        schedule(event.texture);
    }

    private void schedule(ThreadDownloadImageData texture) {
        scheduledTextures.add(texture);
        MC.addScheduledTask(texture::getGlTextureId); // upload the texture to the gpu
    }

    @SubscribeEvent
    public void onSkinDownload(SkinDownloadEvent event) {
        EXECUTOR.submit(() -> event.thread.run());
    }
}
