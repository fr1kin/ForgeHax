package com.matt.forgehax.mods;

import com.matt.forgehax.Globals;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.Switch.Handle;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.entity.LocalPlayerUtils;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.matt.forgehax.Globals.*;

@RegisterMod
public class ElytraFlight extends ToggleMod {
  
  public final Setting<Boolean> fly_on_enable =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("fly_on_enable")
          .description("Start flying when enabled")
          .defaultTo(false)
          .build();
  
  public final Setting<Double> speed =
      getCommandStub()
          .builders()
          .<Double>newSettingBuilder()
          .name("speed")
          .description("Movement speed")
          .defaultTo(0.05D)
          .build();
  
  private final Handle flying = LocalPlayerUtils.getFlySwitch().createHandle(getModName());
  
  public ElytraFlight() {
    super(Category.PLAYER, "ElytraFlight", false, "Elytra Flight");
  }
  
  @Override
  protected void onEnabled() {
    if (fly_on_enable.get()) {
      addScheduledTask(() -> {
        if (getLocalPlayer() != null && !getLocalPlayer().isElytraFlying()) {
          sendNetworkPacket(new CEntityActionPacket(getLocalPlayer(), CEntityActionPacket.Action.START_FALL_FLYING));
        }
      });
    }
  }
  
  @Override
  public void onDisabled() {
    flying.disable();
    // Are we still here?
    if (getLocalPlayer() != null) {
      // Ensure the player starts flying again.
      sendNetworkPacket(new CEntityActionPacket(getLocalPlayer(), CEntityActionPacket.Action.START_FALL_FLYING));
    }
  }
  
  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    // Enable our flight as soon as the player starts flying his elytra.
    if (getLocalPlayer().isElytraFlying()) {
      flying.enable();
    }
    getLocalPlayer().abilities.setFlySpeed(speed.getAsFloat());
  }
}
