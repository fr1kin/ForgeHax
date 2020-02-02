package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.command.Setting;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CMoveVehiclePacket;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
        .processor(data -> Common.addScheduledTask(() -> {
          if (!isEnabled()) {
            Common.printWarning("Mod not enabled");
            return;
          }
          
          if (Common.getLocalPlayer() == null || Common.getWorld() == null) {
            Common.printWarning("Must be ingame to use this command.");
            return;
          }
          
          if (dismountedEntity == null) {
            Common.printWarning("No entity mounted");
            return;
          }

          Common.getWorld().addEntity(dismountedEntity);
          Common.getLocalPlayer().startRiding(dismountedEntity);
          
          Common.printInform("Remounted entity " + dismountedEntity.getName());
        }))
        .build();
    
    getCommandStub().builders().newCommandBuilder()
        .name("dismount")
        .description("Dismount entity")
        .processor(data -> Common.addScheduledTask(() -> {
          if (!isEnabled()) {
            Common.printWarning("Mod not enabled");
            return;
          }
          
          if (Common.getLocalPlayer() == null || Common.getWorld() == null) {
            Common.printWarning("Must be ingame to use this command.");
            return;
          }
          
          Entity mounted = Common.getLocalPlayer().getRidingEntity();
          
          if (mounted == null) {
            Common.printWarning("No entity mounted");
            return;
          }
          
          dismountedEntity = mounted;
          Common.getLocalPlayer().stopRiding();
          mounted.remove();
          
          if (auto_update.get()) {
            forceUpdate = true;
            Common.printInform("Dismounted entity " + mounted.getName() + " and forcing entity updates");
          } else {
            Common.printInform("Dismounted entity " + mounted.getName());
          }
        }))
        .build();
    
    getCommandStub().builders().newCommandBuilder()
        .name("force-update")
        .description("Force dismount entity")
        .processor(data -> Common.addScheduledTask(() -> {
          if (!isEnabled()) {
            Common.printWarning("Mod not enabled");
            return;
          }
          
          if (Common.getLocalPlayer() == null || Common.getWorld() == null) {
            Common.printWarning("Must be ingame to use this command.");
            return;
          }
          
          if (dismountedEntity == null) {
            Common.printWarning("No entity to force remount");
            return;
          }
          
          forceUpdate = !forceUpdate;
          
          Common.printInform("Force mounted entity = %s", forceUpdate ? "true" : "false");
        }))
        .build();
    
    getCommandStub().builders().newCommandBuilder()
        .name("reset")
        .description("Reset the currently stored riding entity")
        .processor(data -> Common.addScheduledTask(() -> {
          this.dismountedEntity = null;
          this.forceUpdate = false;
          Common.printInform("Saved riding entity reset");
        }))
        .build();
  }
  
  @SubscribeEvent
  public void onTick(LocalPlayerUpdateEvent event) {
    if (dismountedEntity == null || Common.getMountedEntity() != null) {
      this.dismountedEntity = null;
      this.forceUpdate = false;
      return;
    }
    
    if (forceUpdate && dismountedEntity != null) {
      dismountedEntity.setPosition(Common.getLocalPlayer().getPosX(), Common.getLocalPlayer().getPosY(), Common.getLocalPlayer().getPosZ());
      Common.sendNetworkPacket(new CMoveVehiclePacket(dismountedEntity));
    }
  }
  
  @SubscribeEvent
  public void onWorldUnload(WorldEvent.Unload event) {
    this.dismountedEntity = null;
    this.forceUpdate = false;
  }
}
