package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.common.ForgeHaxHooks;
import dev.fiki.forgehax.common.events.RenderBoatEvent;
import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.events.ClientTickEvent;
import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.main.util.cmd.settings.DoubleSetting;
import dev.fiki.forgehax.main.util.entity.EntityUtils;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.util.MovementInput;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
    ForgeHaxHooks.isNoBoatGravityActivated =
        Common.getMountedEntity() instanceof BoatEntity; // disable gravity if in boat
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
    ForgeHaxHooks.isNoClampingActivated = noClamp.getValue();
  }

  @SubscribeEvent
  public void onRenderBoat(RenderBoatEvent event) {
    if (EntityUtils.isDrivenByPlayer(event.getBoat()) && setYaw.getValue()) {
      float yaw = Common.getLocalPlayer().rotationYaw;
      event.getBoat().rotationYaw = yaw;
      event.setYaw(yaw);
    }
  }

  @SubscribeEvent
  public void onClientTick(ClientTickEvent.Pre event) {
    // check if the player is really riding a entity
    if (Common.getLocalPlayer() != null && Common.getMountedEntity() != null) {

      ForgeHaxHooks.isNoClampingActivated = noClamp.getValue();
      ForgeHaxHooks.isNoBoatGravityActivated = noGravity.getValue();
      ForgeHaxHooks.isBoatSetYawActivated = setYaw.getValue();

      double velX, velY, velZ;

      if (Common.getGameSettings().keyBindJump.isKeyDown()) {
        // trick the riding entity to think its onground
        Common.getMountedEntity().onGround = false;

        // teleport up
        velY = Common.getGameSettings().keyBindSprint.isKeyDown() ? 5.D : 1.5D;
      } else {
        velY = Common.getGameSettings().keyBindSprint.isKeyDown() ? -1.0 : -speedY.getValue();
      }

      MovementInput movementInput = Common.getLocalPlayer().movementInput;
      double forward = movementInput.moveForward;
      double strafe = movementInput.moveStrafe;
      float yaw = Common.getLocalPlayer().rotationYaw;

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

      Common.getMountedEntity().setMotion(velX, velY, velZ);
    }
  }
}
