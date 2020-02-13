package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.common.ForgeHaxHooks;
import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.cmd.flag.EnumFlag;
import dev.fiki.forgehax.main.util.cmd.settings.IntegerSetting;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import net.minecraftforge.common.ForgeConfig;

// TODO: 1.15
//@RegisterMod
public class XrayMod extends ToggleMod {

  public final IntegerSetting opacity = newIntegerSetting()
      .name("opacity")
      .description("Xray opacity")
      .defaultTo(150)
      .min(0)
      .max(255)
      .flag(EnumFlag.EXECUTOR_MAIN_THREAD)
      .changedListener(
          (from, to) -> {
            ForgeHaxHooks.COLOR_MULTIPLIER_ALPHA = (to / 255.f);
            Common.reloadChunkSmooth();
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

    ForgeHaxHooks.COLOR_MULTIPLIER_ALPHA = (this.opacity.getValue() / 255.f);
    ForgeHaxHooks.SHOULD_UPDATE_ALPHA = true;
    Common.reloadChunkSmooth();
    ForgeHaxHooks.SHOULD_DISABLE_CAVE_CULLING.enable("Xray");
  }

  @Override
  public void onDisabled() {
    ForgeConfig.CLIENT.forgeLightPipelineEnabled.set(previousForgeLightPipelineEnabled);
    ForgeHaxHooks.SHOULD_UPDATE_ALPHA = false;
    Common.reloadChunkSmooth();
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
