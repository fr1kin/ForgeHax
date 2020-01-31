package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.Globals;
import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.command.Setting;
import dev.fiki.forgehax.main.util.entity.LocalPlayerUtils;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.util.Switch.Handle;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
      Globals.addScheduledTask(() -> {
        if (Globals.getLocalPlayer() != null && !Globals.getLocalPlayer().isElytraFlying()) {
          Globals.sendNetworkPacket(new CEntityActionPacket(Globals.getLocalPlayer(), CEntityActionPacket.Action.START_FALL_FLYING));
        }
      });
    }
  }
  
  @Override
  public void onDisabled() {
    flying.disable();
    // Are we still here?
    if (Globals.getLocalPlayer() != null) {
      // Ensure the player starts flying again.
      Globals.sendNetworkPacket(new CEntityActionPacket(Globals.getLocalPlayer(), CEntityActionPacket.Action.START_FALL_FLYING));
    }
  }
  
  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    // Enable our flight as soon as the player starts flying his elytra.
    if (Globals.getLocalPlayer().isElytraFlying()) {
      flying.enable();
    }
    Globals.getLocalPlayer().abilities.setFlySpeed(speed.getAsFloat());
  }
}
