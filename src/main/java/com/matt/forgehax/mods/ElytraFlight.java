package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getNetworkManager;
import com.matt.forgehax.asm.events.PacketEvent;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.events.PlayerTravelEvent;
import com.matt.forgehax.util.Switch.Handle;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.entity.LocalPlayerUtils;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.util.math.MathHelper;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketEntityMetadata;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class ElytraFlight extends ToggleMod {
  
  public enum ElytraMode {
    FLY,
    PACKET,
    CONTROL
  }

  
  public final Setting<Boolean> fly_on_enable =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("fly-on-enable")
          .description("Start flying when enabled")
          .defaultTo(false)
          .build();

  public final Setting<Double> speed =
      getCommandStub()
          .builders()
          .<Double>newSettingBuilder()
          .name("speed")
          .description("Base flight speed")
          .defaultTo(0.0825D)
          .build();

  public final Setting<Float> boost =
      getCommandStub()
          .builders()
          .<Float>newSettingBuilder()
          .name("boost")
          .description("Acceleration amount")
          .defaultTo(0.05F)
          .build();

  public final Setting<Double> maxboost =
      getCommandStub()
          .builders()
          .<Double>newSettingBuilder()
          .name("maxboost")
          .description("Max speed in control mode")
          .defaultTo(1.60D)
          .build();
  
  public final Setting<Double> up_speed =
      getCommandStub()
          .builders()
          .<Double>newSettingBuilder()
          .name("up-speed")
          .description("To keep altitude")
          .defaultTo(0.002D)
          .build();

  public final Setting<Double> down_speed =
      getCommandStub()
          .builders()
          .<Double>newSettingBuilder()
          .name("down-speed")
          .description("Downward speed")
          .defaultTo(0.05D)
          .build();

  public final Setting<ElytraMode> mode =
      getCommandStub()
          .builders()
          .<ElytraMode>newSettingEnumBuilder()
          .name("mode")
          .description("control, fly or packet")
          .defaultTo(ElytraMode.CONTROL)
          .build();
  
  private final Handle flying = LocalPlayerUtils.getFlySwitch().createHandle(getModName());
  
  public ElytraFlight() {
    super(Category.MOVEMENT, "ElytraFlight", false, "Elytra Flight");
  }
  
  private double keep_y = 120;

  @Override
  public String getDisplayText() {
    return (getModName() + " [" + mode.get() + "]");
  }
  
  @Override
  protected void onEnabled() {
	switch(mode.get()) {
	  case CONTROL:
	  case FLY:
    	if (fly_on_enable.get()) {
    	  MC.addScheduledTask(
    	      () -> {
    	        if (getLocalPlayer() != null && !getLocalPlayer().isElytraFlying()) {
    	          getNetworkManager()
    	              .sendPacket(new CPacketEntityAction(getLocalPlayer(), Action.START_FALL_FLYING));
    	        }
    	      });
		}
		break;
	  case PACKET:
		MC.player.capabilities.isFlying = true;
        MC.player.capabilities.allowFlying = true;
    	getLocalPlayer().capabilities.setFlySpeed(speed.getAsFloat());
		break;
    }
  }
  
  @Override
  public void onDisabled() {
	switch(mode.get()) {
	  case FLY:
    	flying.disable(); // No break! I want it to do next stuff too
	  case CONTROL:
    	if (getLocalPlayer().capabilities.isCreativeMode) return;
    	// Are we still here?
    	if (getLocalPlayer() != null) {
    	  // Ensure the player starts flying again.
    	  getNetworkManager()
    	      .sendPacket(new CPacketEntityAction(getLocalPlayer(), Action.START_FALL_FLYING));
    	}
		break;
	  case PACKET:
		MC.player.capabilities.isFlying = false;
        MC.player.capabilities.allowFlying = false;
    	getNetworkManager()
    	    .sendPacket(new CPacketEntityAction(getLocalPlayer(), Action.START_FALL_FLYING));
		break;
	}
  }
  
  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
	switch(mode.get()) {
	  case FLY:
    	// Enable our flight as soon as the player starts flying his elytra.
    	if (getLocalPlayer().isElytraFlying()) {
    	  flying.enable();
    	}
    	getLocalPlayer().capabilities.setFlySpeed(speed.getAsFloat());
		break;
	  case PACKET:
		getNetworkManager()
    	    .sendPacket(new CPacketEntityAction(getLocalPlayer(), Action.START_FALL_FLYING));
		MC.player.capabilities.isFlying = true;
        MC.player.jumpMovementFactor = up_speed.getAsFloat();
		if (!MC.gameSettings.keyBindForward.isKeyDown() && !MC.gameSettings.keyBindBack.isKeyDown() &&
			!MC.gameSettings.keyBindLeft.isKeyDown() && !MC.gameSettings.keyBindRight.isKeyDown())
        	MC.player.setVelocity(0.0, 0.0, 0.0);
		break;
	  case CONTROL:
    	if (getLocalPlayer().isElytraFlying()) {
    	  if(getLocalPlayer().isInWater()) {
    	      getNetworkManager()
				.sendPacket(new CPacketEntityAction(getLocalPlayer(),Action.START_FALL_FLYING));
    	  }
    	  if(!MC.gameSettings.keyBindJump.isKeyDown() && !MC.gameSettings.keyBindSneak.isKeyDown() && 
			 !MC.gameSettings.keyBindForward.isKeyDown() && !MC.gameSettings.keyBindBack.isKeyDown())
			MC.player.setVelocity(0, 0, 0);

    	  if(MC.gameSettings.keyBindJump.isKeyDown()) {
    	    getLocalPlayer().motionY = up_speed.get();
		  }
    	  else if(MC.gameSettings.keyBindSneak.isKeyDown()) {
    	    getLocalPlayer().motionY = -down_speed.get();
		  }
		  else {
    	    getLocalPlayer().motionY = 0;
		  }

    	  float yaw = (float)Math
    	          .toRadians(getLocalPlayer().rotationYaw);
    	  if(MC.gameSettings.keyBindForward.isKeyDown()) {
    	    getLocalPlayer().motionX -= MathHelper.sin(yaw) * boost.get();
    	    getLocalPlayer().motionZ += MathHelper.cos(yaw) * boost.get();
    	  } else if (MC.gameSettings.keyBindBack.isKeyDown()) {
    	    getLocalPlayer().motionX += MathHelper.sin(yaw) * boost.get();
    	    getLocalPlayer().motionZ -= MathHelper.cos(yaw) * boost.get();
    	  }
    	  if(MC.gameSettings.keyBindLeft.isKeyDown()) {
    	    getLocalPlayer().motionX -= MathHelper.sin(yaw - 90.0F) * boost.get();
    	    getLocalPlayer().motionZ += MathHelper.cos(yaw - 90.0F) * boost.get();
    	  } else if (MC.gameSettings.keyBindRight.isKeyDown()) {
    	    getLocalPlayer().motionX -= MathHelper.sin(yaw + 90.0F) * boost.get();
    	    getLocalPlayer().motionZ += MathHelper.cos(yaw + 90.0F) * boost.get();
    	  }
		  double speed = Math.sqrt(Math.pow(getLocalPlayer().motionZ, 2) + Math.pow(getLocalPlayer().motionX, 2));
		  if (speed > maxboost.get()) {
			double factor = maxboost.get() / speed;
			getLocalPlayer().motionX *= factor;
			getLocalPlayer().motionZ *= factor;
		  } 
		}
		break;
	}
  }
}
