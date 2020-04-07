package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static net.minecraftforge.client.ForgeHooksClient.getOffsetFOV;

import com.matt.forgehax.Helper;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.key.BindingHelper;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class Zoom extends ToggleMod {
  
  // 44 = Z
  private final KeyBinding zoomBind = new KeyBinding("Zoom (Camera)", 44, "ForgeHax");
  private final AtomicBoolean lastPressedState = new AtomicBoolean(false);
  
  public Zoom() {
    super(Category.RENDER, "Zoom", !FMLClientHandler.instance().hasOptifine(), "Camera zoom, Optifine-like");
    this.zoomBind.setKeyConflictContext(BindingHelper.getEmptyKeyConflictContext());
  
    ClientRegistry.registerKeyBinding(this.zoomBind);
  }
  
  private final Setting<Float> modifier =
    getCommandStub()
      .builders()
      .<Float>newSettingBuilder()
      .name("modifier")
      .description("multiplies current FoV by value")
      .defaultTo(0.2f)
      .max(2f)
      .min(0.1f) // limited somewhere internally at +- this value
      .build();
  
  @Override
  public boolean isHidden() { return true; }
  
  @SubscribeEvent
  public void onFovUpdate(FOVUpdateEvent ev) {
    if (lastPressedState.get()) {
      ev.setNewfov(ev.getNewfov() * modifier.get());
    }
  }
  
  @SubscribeEvent
  public void onUpdate(LocalPlayerUpdateEvent ev) {
    boolean isDown = zoomBind.isKeyDown();
    if (isDown != lastPressedState.get() && Helper.getWorld() != null) {
      lastPressedState.set(isDown);
      getOffsetFOV(getLocalPlayer(), MC.gameSettings.fovSetting);
    }
  }
}
