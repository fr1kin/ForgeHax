package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.events.ClientTickEvent;
import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.Globals;
import dev.fiki.forgehax.main.util.command.Setting;
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
  
  private final Setting<Integer> delay =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("delay")
          .description("wait ticks before respawning")
          .min(0)
          .defaultTo(50)
          .build();
  
  private boolean isDead = false;
  private int deadTicks = 0;
  
  @SubscribeEvent
  public void onClientTick(ClientTickEvent.Post ev) {
    if (isDead) {
      deadTicks++;
      if (deadTicks > delay.getAsInteger()) {
        deadTicks = 0;
        isDead = false;
        Globals.getLocalPlayer().respawnPlayer();
      }
    }
  }
  
  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    if (Globals.getLocalPlayer().getHealth() <= 0) {
      // TODO: does this even work???
      if (!isDead) { // print once
        Globals.printInform("Died at %.1f, %.1f, %.1f on %s",
            Globals.getLocalPlayer().getPosX(),
            Globals.getLocalPlayer().getPosY(),
            Globals.getLocalPlayer().getPosZ(),
            new SimpleDateFormat("HH:mm:ss").format(new Date())
        );
      }
      isDead = true;
    }
  }
}
