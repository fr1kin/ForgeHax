package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.common.ForgeHaxHooks;
import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.command.Setting;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraftforge.common.ForgeConfig;

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
                Common.reloadChunks();
              })
          .build();
  
  private boolean previousForgeLightPipelineEnabled = false;
  
  public XrayMod() {
    super(Category.WORLD, "Xray", false, "See blocks through walls");
  }
  
  @Override
  public void onEnabled() {
    previousForgeLightPipelineEnabled = ForgeConfig.CLIENT.forgeLightPipelineEnabled.get();
    ForgeConfig.CLIENT.forgeLightPipelineEnabled.set(false);

    ForgeHaxHooks.COLOR_MULTIPLIER_ALPHA = (this.opacity.getAsFloat() / 255.f);
    ForgeHaxHooks.SHOULD_UPDATE_ALPHA = true;
    Common.reloadChunks();
    ForgeHaxHooks.SHOULD_DISABLE_CAVE_CULLING.enable("Xray");
  }
  
  @Override
  public void onDisabled() {
    ForgeConfig.CLIENT.forgeLightPipelineEnabled.set(previousForgeLightPipelineEnabled);
    ForgeHaxHooks.SHOULD_UPDATE_ALPHA = false;
    Common.reloadChunks();
    ForgeHaxHooks.SHOULD_DISABLE_CAVE_CULLING.disable("Xray");
  }
  
  private boolean isInternalCall = false;

//  @SubscribeEvent
//  public void onPreRenderBlockLayer(RenderBlockLayerEvent.Pre event) {
//    if (!isInternalCall) {
//      if (!event.getRenderLayer().equals(Render.TRANSLUCENT)) {
//        event.setCanceled(true);
//      } else if (event.getRenderLayer().equals(BlockRenderLayer.TRANSLUCENT)) {
//        isInternalCall = true;
//        Entity renderEntity = MC.getRenderViewEntity();
//        GlStateManager.disableAlpha();
//        MC.renderGlobal.renderBlockLayer(
//            BlockRenderLayer.SOLID, event.getPartialTicks(), 0, renderEntity);
//        GlStateManager.enableAlpha();
//        MC.renderGlobal.renderBlockLayer(
//            BlockRenderLayer.CUTOUT_MIPPED, event.getPartialTicks(), 0, renderEntity);
//        MC.getTextureManager()
//            .getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE)
//            .setBlurMipmap(false, false);
//        MC.renderGlobal.renderBlockLayer(
//            BlockRenderLayer.CUTOUT, event.getPartialTicks(), 0, renderEntity);
//        MC.getTextureManager()
//            .getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE)
//            .restoreLastBlurMipmap();
//        GlStateManager.disableAlpha();
//        isInternalCall = false;
//      }
//    }
//  }
//
//  @SubscribeEvent
//  public void onPostRenderBlockLayer(RenderBlockLayerEvent.Post event) {
//  }
//
//  @SubscribeEvent
//  public void onRenderBlockInLayer(RenderBlockInLayerEvent event) {
//    if (event.getCompareToLayer().equals(BlockRenderLayer.TRANSLUCENT)) {
//      event.setLayer(event.getCompareToLayer());
//    }
//  } // TODO: 1.15 pretty much entire render engine has been redone
}
