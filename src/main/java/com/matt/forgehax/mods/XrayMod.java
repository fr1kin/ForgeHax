package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.reloadChunks;

import com.matt.forgehax.asm.ForgeHaxHooks;
import com.matt.forgehax.asm.events.RenderBlockInLayerEvent;
import com.matt.forgehax.asm.events.RenderBlockLayerEvent;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod
public class XrayMod extends ToggleMod {
  public final Setting<Integer> opacity =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("opacity")
          .description("Xray opacity")
          .defaultTo(150)
          .min(0)
          .max(255)
          .changed(
              cb -> {
                ForgeHaxHooks.COLOR_MULTIPLIER_ALPHA = (cb.getTo().floatValue() / 255.f);
                reloadChunks();
              })
          .build();

  private boolean previousForgeLightPipelineEnabled = false;

  public XrayMod() {
    super(Category.WORLD, "Xray", false, "See blocks through walls");
  }

  @Override
  public void onEnabled() {
    previousForgeLightPipelineEnabled = ForgeModContainer.forgeLightPipelineEnabled;
    ForgeModContainer.forgeLightPipelineEnabled = false;
    ForgeHaxHooks.COLOR_MULTIPLIER_ALPHA = (this.opacity.getAsFloat() / 255.f);
    ForgeHaxHooks.SHOULD_UPDATE_ALPHA = true;
    reloadChunks();
    ForgeHaxHooks.SHOULD_DISABLE_CAVE_CULLING.enable("Xray");
  }

  @Override
  public void onDisabled() {
    ForgeModContainer.forgeLightPipelineEnabled = previousForgeLightPipelineEnabled;
    ForgeHaxHooks.SHOULD_UPDATE_ALPHA = false;
    reloadChunks();
    ForgeHaxHooks.SHOULD_DISABLE_CAVE_CULLING.disable("Xray");
  }

  private boolean isInternalCall = false;

  @SubscribeEvent
  public void onPreRenderBlockLayer(RenderBlockLayerEvent.Pre event) {
    if (!isInternalCall) {
      if (!event.getRenderLayer().equals(BlockRenderLayer.TRANSLUCENT)) {
        event.setCanceled(true);
      } else if (event.getRenderLayer().equals(BlockRenderLayer.TRANSLUCENT)) {
        isInternalCall = true;
        Entity renderEntity = MC.getRenderViewEntity();
        GlStateManager.disableAlphaTest();
        MC.renderGlobal.renderBlockLayer(BlockRenderLayer.SOLID, event.getPartialTicks(), renderEntity);
        GlStateManager.enableAlphaTest();
        MC.renderGlobal.renderBlockLayer(
            BlockRenderLayer.CUTOUT_MIPPED, event.getPartialTicks(), renderEntity);
        MC.getTextureManager()
            .getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE)
            .setBlurMipmap(false, false);
        MC.renderGlobal.renderBlockLayer(
            BlockRenderLayer.CUTOUT, event.getPartialTicks(), renderEntity);
        MC.getTextureManager()
            .getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE)
            .restoreLastBlurMipmap();
        GlStateManager.disableAlphaTest();
        isInternalCall = false;
      }
    }
  }

  @SubscribeEvent
  public void onPostRenderBlockLayer(RenderBlockLayerEvent.Post event) {}

  @SubscribeEvent
  public void onRenderBlockInLayer(RenderBlockInLayerEvent event) {
    if (event.getCompareToLayer().equals(BlockRenderLayer.TRANSLUCENT)) {
      event.setLayer(event.getCompareToLayer());
    }
  }
}
