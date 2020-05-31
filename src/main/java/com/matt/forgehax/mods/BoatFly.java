package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getRidingEntity;

import com.matt.forgehax.asm.ForgeHaxHooks;
import com.matt.forgehax.asm.events.RenderBoatEvent;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.util.MovementInput;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@RegisterMod
public class BoatFly extends ToggleMod {
  
  public final Setting<Double> speed =
      getCommandStub()
          .builders()
          .<Double>newSettingBuilder()
          .name("speed")
          .description("how fast to move")
          .defaultTo(5.0D)
          .build();
  /*public final Setting<Double> maintainY = getCommandStub().builders().<Double>newSettingBuilder()
  .name("YLevel").description("automatically teleport back up to this Y level").defaultTo(0.0D).build();*/
  public final Setting<Double> speedY =
      getCommandStub()
          .builders()
          .<Double>newSettingBuilder()
          .name("FallSpeed")
          .description("how slowly to fall")
          .defaultTo(0.033D)
          .build();
  
  public final Setting<Boolean> setYaw =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("SetYaw")
          .description("set the boat yaw")
          .defaultTo(true)
          .build();
  public final Setting<Boolean> noClamp =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("NoClamp")
          .description("clamp view angles")
          .defaultTo(true)
          .build();
  public final Setting<Boolean> noGravity =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("NoGravity")
          .description("disable boat gravity")
          .defaultTo(true)
          .build();
  
  public BoatFly() {
    super(Category.MOVEMENT, "BoatFly", false, "Boathax");
  }
  
  @SubscribeEvent // disable gravity
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    ForgeHaxHooks.isNoBoatGravityActivated =
        getRidingEntity() instanceof EntityBoat; // disable gravity if in boat
  }
  
  @Override
  public void onDisabled() {
    // ForgeHaxHooks.isNoClampingActivated = false; // disable view clamping
    ForgeHaxHooks.isNoBoatGravityActivated = false; // disable gravity
    ForgeHaxHooks.isBoatSetYawActivated = false;
    // ForgeHaxHooks.isNotRowingBoatActivated = false; // items always usable - can not be disabled
  }
  
  @Override
  public void onLoad() {
    ForgeHaxHooks.isNoClampingActivated = noClamp.getAsBoolean();
  }
  
  @SubscribeEvent
  public void onRenderBoat(RenderBoatEvent event) {
    if (EntityUtils.isDrivenByPlayer(event.getBoat()) && setYaw.getAsBoolean()) {
      float yaw = getLocalPlayer().rotationYaw;
      event.getBoat().rotationYaw = yaw;
      event.setYaw(yaw);
    }
  }
  
  @SubscribeEvent
  public void onClientTick(TickEvent.ClientTickEvent event) {
    // check if the player is really riding a entity
    if (MC.player != null && MC.player.getRidingEntity() != null) {
      
      ForgeHaxHooks.isNoClampingActivated = noClamp.getAsBoolean();
      ForgeHaxHooks.isNoBoatGravityActivated = noGravity.getAsBoolean();
      ForgeHaxHooks.isBoatSetYawActivated = setYaw.getAsBoolean();
      
      if (MC.gameSettings.keyBindJump.isKeyDown()) {
        // trick the riding entity to think its onground
        MC.player.getRidingEntity().onGround = false;
        
        // teleport up
        MC.player.getRidingEntity().motionY = MC.gameSettings.keyBindSprint.isKeyDown() ? 5 : 1.5;
      } else {
        MC.player.getRidingEntity().motionY =
            MC.gameSettings.keyBindSprint.isKeyDown() ? -1.0 : -speedY.getAsDouble();
      }

      /*if ((MC.player.posY <= maintainY.getAsDouble()-5D) && (MC.player.posY > maintainY.getAsDouble()-10D) && maintainY.getAsDouble() != 0D)
      MC.player.getRidingEntity().setPositionAndUpdate(MC.player.posX, maintainY.getAsDouble(), MC.player.posZ );*/
      
      setMoveSpeedEntity(speed.getAsDouble());
    }
  }
  
  public static void setMoveSpeedEntity(double speed) {
    if (MC.player != null && MC.player.getRidingEntity() != null) {
      MovementInput movementInput = MC.player.movementInput;
      double forward = movementInput.moveForward;
      double strafe = movementInput.moveStrafe;
      float yaw = MC.player.rotationYaw;
      
      if ((forward == 0.0D) && (strafe == 0.0D)) {
        MC.player.getRidingEntity().motionX = (0.0D);
        MC.player.getRidingEntity().motionZ = (0.0D);
      } else {
        if (forward != 0.0D) {
          if (strafe > 0.0D) {
            yaw += (forward > 0.0D ? -45 : 45);
          } else if (strafe < 0.0D) {
            yaw += (forward > 0.0D ? 45 : -45);
          }
          
          strafe = 0.0D;
          
          if (forward > 0.0D) {
            forward = 1.0D;
          } else if (forward < 0.0D) {
            forward = -1.0D;
          }
        }
        MC.player.getRidingEntity().motionX =
            (forward * speed * Math.cos(Math.toRadians(yaw + 90.0F))
                + strafe * speed * Math.sin(Math.toRadians(yaw + 90.0F)));
        MC.player.getRidingEntity().motionZ =
            (forward * speed * Math.sin(Math.toRadians(yaw + 90.0F))
                - strafe * speed * Math.cos(Math.toRadians(yaw + 90.0F)));
      }
    }
  }
}
