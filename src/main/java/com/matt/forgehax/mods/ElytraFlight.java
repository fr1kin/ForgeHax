package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getNetworkManager;
import com.matt.forgehax.asm.events.PacketEvent;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.Switch.Handle;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.entity.LocalPlayerUtils;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.util.math.MathHelper;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.network.play.server.SPacketEntityMetadata;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@RegisterMod
public class ElytraFlight extends ToggleMod {
  
  public final Setting<Boolean> fly_on_enable =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("fly_on_enable")
          .description("Start flying when enabled")
          .defaultTo(false)
          .build();

  public final Setting<Double> speed =
      getCommandStub()
          .builders()
          .<Double>newSettingBuilder()
          .name("speed")
          .description("Base or max flight speed")
          .defaultTo(0.05D)
          .build();
  
  public final Setting<Double> up_speed =
      getCommandStub()
          .builders()
          .<Double>newSettingBuilder()
          .name("up_speed")
          .description("To keep altitude")
          .defaultTo(0.002D)
          .build();

  public final Setting<Double> down_speed =
      getCommandStub()
          .builders()
          .<Double>newSettingBuilder()
          .name("down_speed")
          .description("Downward speed")
          .defaultTo(0.1D)
          .build();

  public final Setting<String> mode =
      getCommandStub()
          .builders()
          .<String>newSettingBuilder()
          .name("mode")
          .description("control, fly or packet")
          .defaultTo("control")
          .build();
  
  private final Handle flying = LocalPlayerUtils.getFlySwitch().createHandle(getModName());
  
  public ElytraFlight() {
    super(Category.MOVEMENT, "ElytraFlight", false, "Elytra Flight");
  }

  @Override
  public String getDisplayText() {
    return (getModName() + " [" + mode.get() + "]");
  }
  
  @Override
  protected void onEnabled() {
	switch(mode.get()) {
	  case "control":
	  case "fly":
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
	  case "packet":
		MC.player.capabilities.isFlying = true;
        MC.player.capabilities.allowFlying = true;
    	getLocalPlayer().capabilities.setFlySpeed(speed.getAsFloat());
		break;
    }
  }
  
  @Override
  public void onDisabled() {
	switch(mode.get()) {
	  case "control":
    	if (getLocalPlayer().capabilities.isCreativeMode) return;
    	getLocalPlayer().capabilities.isFlying = false;
		break;
	  case "fly":
    	flying.disable();
    	// Are we still here?
    	if (getLocalPlayer() != null) {
    	  // Ensure the player starts flying again.
    	  getNetworkManager()
    	      .sendPacket(new CPacketEntityAction(getLocalPlayer(), Action.START_FALL_FLYING));
    	}
		break;
	  case "packet":
		MC.player.capabilities.isFlying = false;
        MC.player.capabilities.allowFlying = false;
    	getNetworkManager()
    	    .sendPacket(new CPacketEntityAction(getLocalPlayer(), Action.START_FALL_FLYING));
		break;
	}
  }

  @SubscribeEvent
  public void onPacketInbound(PacketEvent.Incoming.Pre event) {
    if (this.isEnabled() && mode.get().equals("packet") && 
		event.getPacket() instanceof SPacketEntityMetadata) {
      event.setCanceled(true);
    }
  }
  
  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
	switch(mode.get()) {
	  case "fly":
    	// Enable our flight as soon as the player starts flying his elytra.
    	if (getLocalPlayer().isElytraFlying()) {
    	  flying.enable();
    	}
    	getLocalPlayer().capabilities.setFlySpeed(speed.getAsFloat());
		break;
	  case "packet":
		getNetworkManager()
    	    .sendPacket(new CPacketEntityAction(getLocalPlayer(), Action.START_FALL_FLYING));
		MC.player.capabilities.isFlying = true;
        MC.player.jumpMovementFactor = up_speed.getAsFloat();
		if (!MC.gameSettings.keyBindForward.isKeyDown() && !MC.gameSettings.keyBindBack.isKeyDown())
        	MC.player.setVelocity(0.0, 0.0, 0.0);
		break;
	  case "control":
    	if (!getLocalPlayer().isElytraFlying()) return;
    	  if(getLocalPlayer().isInWater()) {
    	      getNetworkManager()
				.sendPacket(new CPacketEntityAction(getLocalPlayer(),Action.START_FALL_FLYING));
    	      return;
    	  }
    	  if(MC.gameSettings.keyBindJump.isKeyDown())
    	    getLocalPlayer().motionY += up_speed.get();
    	  else if(MC.gameSettings.keyBindSneak.isKeyDown())
    	    getLocalPlayer().motionY -= down_speed.get();
		  else
    	    getLocalPlayer().motionY = 0;

    	  if(MC.gameSettings.keyBindForward.isKeyDown()) {
    	    float yaw = (float)Math
    	            .toRadians(getLocalPlayer().rotationYaw);
    	    getLocalPlayer().motionX -= MathHelper.sin(yaw) * 0.05F;
    	    getLocalPlayer().motionZ += MathHelper.cos(yaw) * 0.05F;
    	  } else if (MC.gameSettings.keyBindBack.isKeyDown()) {
    	    float yaw = (float)Math
    	            .toRadians(MC.player.rotationYaw);
    	    getLocalPlayer().motionX += MathHelper.sin(yaw) * 0.05F;
    	    getLocalPlayer().motionZ -= MathHelper.cos(yaw) * 0.05F;
    	  }
		  if (Math.abs(getLocalPlayer().motionZ) > speed.get())
			getLocalPlayer().motionZ = Math.signum(getLocalPlayer().motionZ) * speed.get();
		  if (Math.abs(getLocalPlayer().motionX) > speed.get())
			getLocalPlayer().motionX = Math.signum(getLocalPlayer().motionX) * speed.get();
		  break;
	}
  }
}
