package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getNetworkManager;
import static com.matt.forgehax.Helper.getWorld;
import static com.matt.forgehax.Helper.printInform;
import static com.matt.forgehax.Helper.printWarning;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPacketVehicleMove;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class RiderDesync extends ToggleMod {
  
  private final Setting<Boolean> auto_update =
    getCommandStub()
      .builders()
      .<Boolean>newSettingBuilder()
      .name("auto-update")
      .description("Automatically update entity on dismount")
      .defaultTo(true)
      .build();
  
  private Entity dismountedEntity = null;
  private boolean forceUpdate = false;
  
  public RiderDesync() {
    super(Category.PLAYER, "RiderDesync", false, "For entity force dismounting");
  }
  
  @Override
  public String getDebugDisplayText() {
    return super.getDebugDisplayText() + String.format(" [e = %s fu = %s]",
      dismountedEntity == null ? "null" : dismountedEntity.getName(),
      forceUpdate ? "true" : "false");
  }
  
  @Override
  protected void onLoad() {
    getCommandStub().builders().newCommandBuilder()
      .name("remount")
      .description("Remount entity")
      .processor(data -> MC.addScheduledTask(() -> {
        if (!isEnabled()) {
          printWarning("Mod not enabled");
          return;
        }
    
        if (getLocalPlayer() == null || getWorld() == null) {
          printWarning("Must be ingame to use this command.");
          return;
        }
    
        if (dismountedEntity == null) {
          printWarning("No entity mounted");
          return;
        }
    
        dismountedEntity.isDead = false;
        getWorld().spawnEntity(dismountedEntity);
        getLocalPlayer().startRiding(dismountedEntity);
    
        printInform("Remounted entity " + dismountedEntity.getName());
      }))
      .build();
    
    getCommandStub().builders().newCommandBuilder()
      .name("dismount")
      .description("Dismount entity")
      .processor(data -> MC.addScheduledTask(() -> {
        if (!isEnabled()) {
          printWarning("Mod not enabled");
          return;
        }
    
        if (getLocalPlayer() == null || getWorld() == null) {
          printWarning("Must be ingame to use this command.");
          return;
        }
    
        Entity mounted = getLocalPlayer().getRidingEntity();
    
        if (mounted == null) {
          printWarning("No entity mounted");
          return;
        }
    
        dismountedEntity = mounted;
        getLocalPlayer().dismountRidingEntity();
        getWorld().removeEntity(mounted);
    
        if (auto_update.get()) {
          forceUpdate = true;
          printInform("Dismounted entity " + mounted.getName() + " and forcing entity updates");
        } else {
          printInform("Dismounted entity " + mounted.getName());
        }
      }))
      .build();
    
    getCommandStub().builders().newCommandBuilder()
      .name("force-update")
      .description("Force dismount entity")
      .processor(data -> MC.addScheduledTask(() -> {
        if (!isEnabled()) {
          printWarning("Mod not enabled");
          return;
        }
    
        if (getLocalPlayer() == null || getWorld() == null) {
          printWarning("Must be ingame to use this command.");
          return;
        }
    
        if (dismountedEntity == null) {
          printWarning("No entity to force remount");
          return;
        }
    
        forceUpdate = !forceUpdate;
    
        printInform("Force mounted entity = %s", forceUpdate ? "true" : "false");
      }))
      .build();
    
    getCommandStub().builders().newCommandBuilder()
      .name("reset")
      .description("Reset the currently stored riding entity")
      .processor(data -> MC.addScheduledTask(() -> {
        this.dismountedEntity = null;
        this.forceUpdate = false;
        printInform("Saved riding entity reset");
      }))
      .build();
  }
  
  @SubscribeEvent
  public void onTick(LocalPlayerUpdateEvent event) {
    if (dismountedEntity == null || getLocalPlayer().isRiding()) {
      this.dismountedEntity = null;
      this.forceUpdate = false;
      return;
    }
  
    if (forceUpdate && dismountedEntity != null) {
      dismountedEntity
        .setPosition(getLocalPlayer().posX, getLocalPlayer().posY, getLocalPlayer().posZ);
      getNetworkManager().sendPacket(new CPacketVehicleMove(dismountedEntity));
    }
  }
  
  @SubscribeEvent
  public void onWorldUnload(WorldEvent.Unload event) {
    this.dismountedEntity = null;
    this.forceUpdate = false;
  }
}
