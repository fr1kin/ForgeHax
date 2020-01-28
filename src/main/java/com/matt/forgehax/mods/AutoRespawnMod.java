package com.matt.forgehax.mods;

import com.matt.forgehax.events.ClientTickEvent;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.matt.forgehax.Globals.*;

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
        getLocalPlayer().respawnPlayer();
      }
    }
  }
  
  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    if (getLocalPlayer().getHealth() <= 0) {
      // TODO: does this even work???
      if (!isDead) { // print once
        printInform("Died at %.1f, %.1f, %.1f on %s",
            getLocalPlayer().getPosX(),
            getLocalPlayer().getPosY(),
            getLocalPlayer().getPosZ(),
            new SimpleDateFormat("HH:mm:ss").format(new Date())
        );
      }
      isDead = true;
    }
  }
}
