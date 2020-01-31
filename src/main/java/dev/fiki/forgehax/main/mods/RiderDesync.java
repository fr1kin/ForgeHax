package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.Globals;
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
        .processor(data -> Globals.addScheduledTask(() -> {
          if (!isEnabled()) {
            Globals.printWarning("Mod not enabled");
            return;
          }
          
          if (Globals.getLocalPlayer() == null || Globals.getWorld() == null) {
            Globals.printWarning("Must be ingame to use this command.");
            return;
          }
          
          if (dismountedEntity == null) {
            Globals.printWarning("No entity mounted");
            return;
          }

          Globals.getWorld().addEntity(dismountedEntity);
          Globals.getLocalPlayer().startRiding(dismountedEntity);
          
          Globals.printInform("Remounted entity " + dismountedEntity.getName());
        }))
        .build();
    
    getCommandStub().builders().newCommandBuilder()
        .name("dismount")
        .description("Dismount entity")
        .processor(data -> Globals.addScheduledTask(() -> {
          if (!isEnabled()) {
            Globals.printWarning("Mod not enabled");
            return;
          }
          
          if (Globals.getLocalPlayer() == null || Globals.getWorld() == null) {
            Globals.printWarning("Must be ingame to use this command.");
            return;
          }
          
          Entity mounted = Globals.getLocalPlayer().getRidingEntity();
          
          if (mounted == null) {
            Globals.printWarning("No entity mounted");
            return;
          }
          
          dismountedEntity = mounted;
          Globals.getLocalPlayer().stopRiding();
          mounted.remove();
          
          if (auto_update.get()) {
            forceUpdate = true;
            Globals.printInform("Dismounted entity " + mounted.getName() + " and forcing entity updates");
          } else {
            Globals.printInform("Dismounted entity " + mounted.getName());
          }
        }))
        .build();
    
    getCommandStub().builders().newCommandBuilder()
        .name("force-update")
        .description("Force dismount entity")
        .processor(data -> Globals.addScheduledTask(() -> {
          if (!isEnabled()) {
            Globals.printWarning("Mod not enabled");
            return;
          }
          
          if (Globals.getLocalPlayer() == null || Globals.getWorld() == null) {
            Globals.printWarning("Must be ingame to use this command.");
            return;
          }
          
          if (dismountedEntity == null) {
            Globals.printWarning("No entity to force remount");
            return;
          }
          
          forceUpdate = !forceUpdate;
          
          Globals.printInform("Force mounted entity = %s", forceUpdate ? "true" : "false");
        }))
        .build();
    
    getCommandStub().builders().newCommandBuilder()
        .name("reset")
        .description("Reset the currently stored riding entity")
        .processor(data -> Globals.addScheduledTask(() -> {
          this.dismountedEntity = null;
          this.forceUpdate = false;
          Globals.printInform("Saved riding entity reset");
        }))
        .build();
  }
  
  @SubscribeEvent
  public void onTick(LocalPlayerUpdateEvent event) {
    if (dismountedEntity == null || Globals.getMountedEntity() != null) {
      this.dismountedEntity = null;
      this.forceUpdate = false;
      return;
    }
    
    if (forceUpdate && dismountedEntity != null) {
      dismountedEntity.setPosition(Globals.getLocalPlayer().getPosX(), Globals.getLocalPlayer().getPosY(), Globals.getLocalPlayer().getPosZ());
      Globals.sendNetworkPacket(new CMoveVehiclePacket(dismountedEntity));
    }
  }
  
  @SubscribeEvent
  public void onWorldUnload(WorldEvent.Unload event) {
    this.dismountedEntity = null;
    this.forceUpdate = false;
  }
}
