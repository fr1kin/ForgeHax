package com.matt.forgehax.mods;

import com.matt.forgehax.asm.ForgeHaxHooks;
import com.matt.forgehax.asm.events.ComputeVisibilityEvent;
import com.matt.forgehax.asm.events.RenderBlockInLayerEvent;
import com.matt.forgehax.asm.events.RenderBlockLayerEvent;
import com.matt.forgehax.asm.events.SetupTerrainEvent;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

public class XrayMod extends ToggleMod {
    public Property opacity;

    private boolean previousForgeLightPipelineEnabled = false;

    public XrayMod() {
        super("Xray", false, "See blocks through walls");
    }

    public void reloadRenderers() {
        if(MC.renderGlobal != null) {
            MC.renderGlobal.loadRenderers();
        }
    }

    @Override
    public void onEnabled() {
        previousForgeLightPipelineEnabled = ForgeModContainer.forgeLightPipelineEnabled;
        ForgeModContainer.forgeLightPipelineEnabled = false;
        ForgeHaxHooks.SHOULD_UPDATE_ALPHA = true;
        reloadRenderers();
    }

    @Override
    public void onDisabled() {
        ForgeModContainer.forgeLightPipelineEnabled = previousForgeLightPipelineEnabled;
        ForgeHaxHooks.SHOULD_UPDATE_ALPHA = false;
        reloadRenderers();
    }

    @Override
    public void loadConfig(Configuration configuration) {
        addSettings(
                opacity = configuration.get(getModName(),
                        "opacity",
                        150,
                        "How transparent the blocks will be",
                        0, 255)
        );
        ForgeHaxHooks.COLOR_MULTIPLIER_ALPHA = (float)(opacity.getDouble() / 255.f);
    }

    @Override
    public void onConfigUpdated(List<Property> changed) {
        if(changed.contains(opacity)) {
            ForgeHaxHooks.COLOR_MULTIPLIER_ALPHA = (float)(opacity.getDouble() / 255.f);
            reloadRenderers();
        }
    }

    private boolean isInternalCall = false;

    @SubscribeEvent
    public void onPreRenderBlockLayer(RenderBlockLayerEvent.Pre event) {
        if(!isInternalCall) {
            if (!event.getRenderLayer().equals(BlockRenderLayer.TRANSLUCENT)) {
                event.setCanceled(true);
            } else if (event.getRenderLayer().equals(BlockRenderLayer.TRANSLUCENT)) {
                isInternalCall = true;
                Entity renderEntity = MC.getRenderViewEntity();
                GlStateManager.disableAlpha();
                MC.renderGlobal.renderBlockLayer(BlockRenderLayer.SOLID, event.getPartialTicks(), 0, renderEntity);
                GlStateManager.enableAlpha();
                MC.renderGlobal.renderBlockLayer(BlockRenderLayer.CUTOUT_MIPPED, event.getPartialTicks(), 0, renderEntity);
                MC.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
                MC.renderGlobal.renderBlockLayer(BlockRenderLayer.CUTOUT, event.getPartialTicks(), 0, renderEntity);
                MC.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
                GlStateManager.disableAlpha();
                isInternalCall = false;
            }
        }
    }

    @SubscribeEvent
    public void onPostRenderBlockLayer(RenderBlockLayerEvent.Post event) {}

    @SubscribeEvent
    public void onRenderBlockInLayer(RenderBlockInLayerEvent event) {
        if(event.getCompareToLayer().equals(BlockRenderLayer.TRANSLUCENT)) {
            event.setLayer(event.getCompareToLayer());
        }
    }

    @SubscribeEvent
    public void onComputeVisibility(ComputeVisibilityEvent event) {
        if(MOD.getMod("NoCaveCulling").getProperty("enabled").getBoolean())
            event.getSetVisibility().setAllVisible(true);
    }
}
