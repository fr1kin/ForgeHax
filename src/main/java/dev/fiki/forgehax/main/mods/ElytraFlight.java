package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.main.util.cmd.settings.DoubleSetting;
import dev.fiki.forgehax.main.util.cmd.settings.FloatSetting;
import dev.fiki.forgehax.main.util.entity.LocalPlayerUtils;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.util.Switch.Handle;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod
public class ElytraFlight extends ToggleMod {

  public final BooleanSetting fly_on_enable = newBooleanSetting()
      .name("fly_on_enable")
      .description("Start flying when enabled")
      .defaultTo(false)
      .build();

  public final FloatSetting speed = newFloatSetting()
      .name("speed")
      .description("Movement speed")
      .defaultTo(0.05f)
      .build();

  private final Handle flying = LocalPlayerUtils.getFlySwitch().createHandle(getName());

  public ElytraFlight() {
    super(Category.PLAYER, "ElytraFlight", false, "Elytra Flight");
  }

  @Override
  protected void onEnabled() {
    if (fly_on_enable.getValue()) {
      Common.addScheduledTask(() -> {
        if (Common.getLocalPlayer() != null && !Common.getLocalPlayer().isElytraFlying()) {
          Common.sendNetworkPacket(new CEntityActionPacket(Common.getLocalPlayer(), CEntityActionPacket.Action.START_FALL_FLYING));
        }
      });
    }
  }

  @Override
  public void onDisabled() {
    flying.disable();
    // Are we still here?
    if (Common.getLocalPlayer() != null) {
      // Ensure the player starts flying again.
      Common.sendNetworkPacket(new CEntityActionPacket(Common.getLocalPlayer(), CEntityActionPacket.Action.START_FALL_FLYING));
    }
  }

  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    // Enable our flight as soon as the player starts flying his elytra.
    if (Common.getLocalPlayer().isElytraFlying()) {
      flying.enable();
    }
    Common.getLocalPlayer().abilities.setFlySpeed(speed.getValue());
  }
}
