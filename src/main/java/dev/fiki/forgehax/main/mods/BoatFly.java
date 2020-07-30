package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.asm.events.RenderBoatEvent;
import dev.fiki.forgehax.asm.events.boat.ClampBoatEvent;
import dev.fiki.forgehax.asm.events.boat.RowBoatEvent;
import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.events.PreClientTickEvent;
import dev.fiki.forgehax.main.util.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.main.util.cmd.settings.DoubleSetting;
import dev.fiki.forgehax.main.util.entity.EntityUtils;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.util.reflection.FastReflection;
import net.minecraft.util.MovementInput;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static dev.fiki.forgehax.main.Common.*;

@RegisterMod
public class BoatFly extends ToggleMod {

  public final DoubleSetting speed = newDoubleSetting()
      .name("speed")
      .description("how fast to move")
      .defaultTo(5.0D)
      .build();

  public final DoubleSetting speedY = newDoubleSetting()
      .name("fall-speed")
      .description("how slowly to fall")
      .defaultTo(0.033D)
      .build();

  public final BooleanSetting setYaw = newBooleanSetting()
      .name("set-yaw")
      .description("set the boat yaw")
      .defaultTo(true)
      .build();

  public final BooleanSetting noClamp = newBooleanSetting()
      .name("no-clamp")
      .description("clamp view angles")
      .defaultTo(true)
      .build();

  public final BooleanSetting noGravity = newBooleanSetting()
      .name("no-gravity")
      .description("disable boat gravity")
      .defaultTo(true)
      .build();

  public BoatFly() {
    super(Category.MISC, "BoatFly", false, "Boathax");
  }

  @SubscribeEvent // disable gravity
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
//    ForgeHaxHooks.isNoBoatGravityActivated =
//        getMountedEntity() instanceof BoatEntity; // disable gravity if in boat
  }

  @SubscribeEvent
  public void onClampBoatAngles(ClampBoatEvent event) {
    event.setCanceled(noClamp.isEnabled());
  }

  @SubscribeEvent
  public void onBoatRowing(RowBoatEvent event) {
    event.setCanceled(true);
  }

  @Override
  public void onDisabled() {
    // ForgeHaxHooks.isNoClampingActivated = false; // disable view clamping
//    ForgeHaxHooks.isNoBoatGravityActivated = false; // disable gravity
//    ForgeHaxHooks.isBoatSetYawActivated = false;
    // ForgeHaxHooks.isNotRowingBoatActivated = false; // items always usable - can not be disabled
  }

  @SubscribeEvent
  public void onRenderBoat(RenderBoatEvent event) {
    if (EntityUtils.isDrivenByPlayer(event.getBoat()) && setYaw.getValue()) {
      float yaw = getLocalPlayer().rotationYaw;
      event.getBoat().rotationYaw = yaw;
      event.setYaw(yaw);
    }
  }

  @SubscribeEvent
  public void onClientTick(PreClientTickEvent event) {
    // check if the player is really riding a entity
    if (getLocalPlayer() != null && getMountedEntity() != null) {

//      ForgeHaxHooks.isNoBoatGravityActivated = noGravity.getValue();
//      ForgeHaxHooks.isBoatSetYawActivated = setYaw.getValue();

      double velX, velY, velZ;

      if (getGameSettings().keyBindJump.isKeyDown()) {
        // trick the riding entity to think its onground
        FastReflection.Fields.Entity_onGround.set(getMountedEntity(), false);

        // teleport up
        velY = getGameSettings().keyBindSprint.isKeyDown() ? 5.D : 1.5D;
      } else {
        velY = getGameSettings().keyBindSprint.isKeyDown() ? -1.0 : -speedY.getValue();
      }

      MovementInput movementInput = getLocalPlayer().movementInput;
      double forward = movementInput.moveForward;
      double strafe = movementInput.moveStrafe;
      float yaw = getLocalPlayer().rotationYaw;

      if ((forward == 0.0D) && (strafe == 0.0D)) {
        velX = velZ = 0.D;
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

        double sin = Math.sin(Math.toRadians(yaw + 90.0F));
        double cos = Math.cos(Math.toRadians(yaw + 90.0F));

        velX = (forward * speed.getValue() * cos + strafe * speed.getValue() * sin);
        velZ = (forward * speed.getValue() * sin - strafe * speed.getValue() * cos);
      }

      getMountedEntity().setMotion(velX, velY, velZ);
    }
  }
}
