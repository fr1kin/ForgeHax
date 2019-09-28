package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;

import com.matt.forgehax.Helper;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import java.text.SimpleDateFormat;
import java.util.Date;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;


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
  public void onClientTick(ClientTickEvent ev) {
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
      if (isDead == false) { // print once
        Helper.printInform("Died at %.1f, %.1f, %.1f on %s",
            getLocalPlayer().posX,
            getLocalPlayer().posY,
            getLocalPlayer().posZ,
            new SimpleDateFormat("HH:mm:ss").format(new Date())
        );
      }
      isDead = true;
    }
  }
}
