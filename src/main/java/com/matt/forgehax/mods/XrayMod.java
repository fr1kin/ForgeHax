package com.matt.forgehax.mods;

import com.matt.forgehax.asm.ForgeHaxHooks;
import com.matt.forgehax.asm.events.ComputeVisibilityEvent;
import com.matt.forgehax.asm.events.RenderBlockInLayerEvent;
import com.matt.forgehax.asm.events.RenderBlockLayerEvent;
import com.matt.forgehax.asm.events.SetupTerrainEvent;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
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

    public XrayMod(String modName, boolean defaultValue, String description, int key) {
        super(modName, defaultValue, description, key);
    }

    public void reloadRenderers(boolean b) {
        if(MC.renderGlobal != null) {
            MC.renderGlobal.loadRenderers();
            //MC.getBlockRendererDispatcher().get
        }
    }

    @Override
    public void onEnabled() {
        previousForgeLightPipelineEnabled = ForgeModContainer.forgeLightPipelineEnabled;
        ForgeModContainer.forgeLightPipelineEnabled = false;
        ForgeHaxHooks.SHOULD_UPDATE_ALPHA = true;
        reloadRenderers(true);
    }

    @Override
    public void onDisabled() {
        ForgeModContainer.forgeLightPipelineEnabled = previousForgeLightPipelineEnabled;
        ForgeHaxHooks.SHOULD_UPDATE_ALPHA = false;
        reloadRenderers(false);
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
        }
    }

    private boolean isInternalCall = false;

    @SubscribeEvent
    public void onPreRenderBlockLayer(RenderBlockLayerEvent.Pre event) {
        if(!event.getRenderLayer().equals(BlockRenderLayer.TRANSLUCENT) && !isInternalCall) {
            event.setCanceled(true);
        } else if(event.getRenderLayer().equals(BlockRenderLayer.TRANSLUCENT) && !isInternalCall){
            GlStateManager.enableDepth();
            isInternalCall = true;
            GlStateManager.disableAlpha();
            MC.renderGlobal.renderBlockLayer(BlockRenderLayer.SOLID, event.getPartialTicks(), 0, MC.getRenderViewEntity());

            isInternalCall = true;
            GlStateManager.enableAlpha();
            MC.renderGlobal.renderBlockLayer(BlockRenderLayer.CUTOUT_MIPPED, event.getPartialTicks(), 0, MC.getRenderViewEntity());

            isInternalCall = true;
            MC.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
            MC.renderGlobal.renderBlockLayer(BlockRenderLayer.CUTOUT, event.getPartialTicks(), 0, MC.getRenderViewEntity());
            MC.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
        }
    }

    @SubscribeEvent
    public void onPostRenderBlockLayer(RenderBlockLayerEvent.Post event) {
        if(!event.getRenderLayer().equals(BlockRenderLayer.TRANSLUCENT)) {
            isInternalCall = false;
        } else {
            GlStateManager.disableDepth();
        }
    }

    @SubscribeEvent
    public void onRenderBlockInLayer(RenderBlockInLayerEvent event) {
        if(event.getLayer().equals(BlockRenderLayer.TRANSLUCENT) && !event.getLayer().equals(event.getBlock().getBlockLayer())) {
            event.setReturnValue(true);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onComputeVisibility(ComputeVisibilityEvent event) {
        event.getSetVisibility().setAllVisible(true);
    }
}
