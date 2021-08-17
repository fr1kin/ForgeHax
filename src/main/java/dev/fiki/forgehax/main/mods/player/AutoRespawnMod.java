package dev.fiki.forgehax.main.mods.player;

import dev.fiki.forgehax.api.cmd.settings.IntegerSetting;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.entity.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.api.events.game.PostGameTickEvent;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.main.Common;

import java.text.SimpleDateFormat;
import java.util.Date;

@RegisterMod(
    name = "AutoRespawn",
    description = "Auto respawn on death",
    category = Category.PLAYER
)
public class AutoRespawnMod extends ToggleMod {
  private final IntegerSetting delay = newIntegerSetting()
      .name("delay")
      .description("wait ticks before respawning")
      .min(0)
      .defaultTo(50)
      .build();

  private boolean isDead = false;
  private int deadTicks = 0;

  @SubscribeListener
  public void onClientTick(PostGameTickEvent ev) {
    if (isDead) {
      deadTicks++;
      if (deadTicks > delay.getValue()) {
        deadTicks = 0;
        isDead = false;
        Common.getLocalPlayer().respawn();
      }
    }
  }

  @SubscribeListener
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    if (Common.getLocalPlayer().getHealth() <= 0) {
      // TODO: does this even work???
      if (!isDead) { // print once
        Common.printInform("Died at %.1f, %.1f, %.1f on %s",
            Common.getLocalPlayer().getX(),
            Common.getLocalPlayer().getY(),
            Common.getLocalPlayer().getZ(),
            new SimpleDateFormat("HH:mm:ss").format(new Date())
        );
      }
      isDead = true;
    }
  }
}
