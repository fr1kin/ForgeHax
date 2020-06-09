package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;

import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.events.RenderEvent;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.block.material.Material;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class AntiOverlayMod extends ToggleMod {

  public final Setting<Boolean> noBossBar =
    getCommandStub()
      .builders()
      .<Boolean>newSettingBuilder()
      .name("boss-bar")
      .description("Won't render wither/dragon boss bar")
      .defaultTo(false)
      .build();

  public final Setting<Boolean> noFire =
    getCommandStub()
      .builders()
      .<Boolean>newSettingBuilder()
      .name("fire")
      .description("Won't render fire overlay")
      .defaultTo(false)
      .build();

  public final Setting<Boolean> noHelmet =
    getCommandStub()
      .builders()
      .<Boolean>newSettingBuilder()
      .name("helmet")
      .description("Won't render helmet")
      .defaultTo(false)
      .build();

  public final Setting<Boolean> noPortal =
    getCommandStub()
      .builders()
      .<Boolean>newSettingBuilder()
      .name("portal")
      .description("Won't render portal effect")
      .defaultTo(false)
      .build();

  public final Setting<Boolean> noTotem =
    getCommandStub()
      .builders()
      .<Boolean>newSettingBuilder()
      .name("totem")
      .description("Won't render totem effect")
      .defaultTo(false)
      .build();

  public final Setting<Boolean> noLiquid =
    getCommandStub()
      .builders()
      .<Boolean>newSettingBuilder()
      .name("liquid")
      .description("Removed liquid fog")
      .defaultTo(false)
      .build();

  public final Setting<Boolean> allOverlay =
    getCommandStub()
      .builders()
      .<Boolean>newSettingBuilder()
      .name("all")
      .description("Won't render any block overlay")
      .defaultTo(false)
      .build();
  
  public AntiOverlayMod() {
    super(Category.RENDER, "AntiOverlay", false, "Removes screen overlays");
  }
  
  /**
   * Disables water/lava fog
   */
  @SubscribeEvent
  public void onFogRender(EntityViewRenderEvent.FogDensity event) {
    if (noLiquid.get() && (event.getState().getMaterial().equals(Material.WATER)
        || event.getState().getMaterial().equals(Material.LAVA))) {
      event.setDensity(0);
      event.setCanceled(true);
    }
  }
  
  /**
   * Disables screen overlays or only fire
   */
  @SubscribeEvent
  public void onRenderBlockOverlay(RenderBlockOverlayEvent event) {
    if (allOverlay.get() || (noFire.get() && event.getPlayer().equals(getLocalPlayer())
        && event.getOverlayType().equals(RenderBlockOverlayEvent.OverlayType.FIRE))) {
      event.setCanceled(true);
    }
  }
  
  /**
   * Disables BossBar, Portal, Helmet
   */
  @SubscribeEvent
  public void onRenderGameOverlay(RenderGameOverlayEvent event) {
    if ((event.getType().equals(RenderGameOverlayEvent.ElementType.HELMET) && noHelmet.get()) ||
        (event.getType().equals(RenderGameOverlayEvent.ElementType.PORTAL) && noPortal.get()) ||
        (event instanceof RenderGameOverlayEvent.BossInfo && noBossBar.get())) {
      event.setCanceled(true);
    }
  }
  
  /**
   * Disables Totem
   */
  @SubscribeEvent
  public void onRender(RenderEvent event) {
    ItemStack item = FastReflection.Fields.EntityRenderer_itemActivationItem.get(MC.entityRenderer);
    
    if (noTotem.get() && item != null && item.getItem() == Items.TOTEM_OF_UNDYING) {
      FastReflection.Fields.EntityRenderer_itemActivationItem.set(MC.entityRenderer, null);
    }
  }
}
