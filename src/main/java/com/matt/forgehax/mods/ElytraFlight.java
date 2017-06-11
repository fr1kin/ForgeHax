package com.matt.forgehax.mods;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static com.matt.forgehax.Wrapper.getLocalPlayer;
import static com.matt.forgehax.Wrapper.getNetworkManager;

@RegisterMod
public class ElytraFlight extends ToggleMod {
	public final Setting<Double> speed = getCommandStub().builders().<Double>newSettingBuilder()
			.name("speed")
			.description("Movement speed")
			.defaultTo(0.05D)
			.build();

	public ElytraFlight() {
		super("ElytraFlight", false, "Elytra Flight");
	}

	@Override
	public void onDisabled() {

		// Are we still here?
		if (getLocalPlayer() != null) {

			// Disable creativeflight.
			getLocalPlayer().capabilities.isFlying = false;

			// Ensure the player starts flying again.
			getNetworkManager().sendPacket(new CPacketEntityAction(getLocalPlayer(), Action.START_FALL_FLYING));
		}

	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
		// Enable our flight as soon as the player starts flying his elytra.
		if (getLocalPlayer().isElytraFlying()) {
			getLocalPlayer().capabilities.isFlying = true;
		}
		getLocalPlayer().capabilities.setFlySpeed(speed.getAsFloat());
	}
}