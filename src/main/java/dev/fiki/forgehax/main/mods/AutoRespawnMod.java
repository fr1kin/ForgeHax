package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.events.PostClientTickEvent;
import dev.fiki.forgehax.main.util.cmd.settings.IntegerSetting;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;

import java.text.SimpleDateFormat;
import java.util.Date;

import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod
public class AutoRespawnMod extends ToggleMod {

  public AutoRespawnMod() {
    super(Category.PLAYER, "AutoRespawn", false, "Auto respawn on death");
  }

  private final IntegerSetting delay = newIntegerSetting()
      .name("delay")
      .description("wait ticks before respawning")
      .min(0)
      .defaultTo(50)
      .build();

  private boolean isDead = false;
  private int deadTicks = 0;

  @SubscribeEvent
  public void onClientTick(PostClientTickEvent ev) {
    if (isDead) {
      deadTicks++;
      if (deadTicks > delay.getValue()) {
        deadTicks = 0;
        isDead = false;
        Common.getLocalPlayer().respawnPlayer();
      }
    }
  }

  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    if (Common.getLocalPlayer().getHealth() <= 0) {
      // TODO: does this even work???
      if (!isDead) { // print once
        Common.printInform("Died at %.1f, %.1f, %.1f on %s",
            Common.getLocalPlayer().getPosX(),
            Common.getLocalPlayer().getPosY(),
            Common.getLocalPlayer().getPosZ(),
            new SimpleDateFormat("HH:mm:ss").format(new Date())
        );
      }
      isDead = true;
    }
  }
}
