package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getNetworkManager;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.Switch.Handle;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.entity.LocalPlayerUtils;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
      MC.addScheduledTask(
        () -> {
          if (getLocalPlayer() != null && !getLocalPlayer().isElytraFlying()) {
            getNetworkManager()
              .sendPacket(new CPacketEntityAction(getLocalPlayer(), Action.START_FALL_FLYING));
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
      getNetworkManager()
        .sendPacket(new CPacketEntityAction(getLocalPlayer(), Action.START_FALL_FLYING));
    }
  }
  
  @SubscribeEvent
  @SideOnly(Side.CLIENT)
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    // Enable our flight as soon as the player starts flying his elytra.
    if (getLocalPlayer().isElytraFlying()) {
      flying.enable();
    }
    getLocalPlayer().capabilities.setFlySpeed(speed.getAsFloat());
  }
}
