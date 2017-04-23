package com.matt.forgehax.mods;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ElytraFlight extends ToggleMod {

	private Property speed;

	public ElytraFlight() {
		super("ElytraFlight", false, "Elytra Flight");
	}

	@Override
	public void loadConfig(Configuration configuration) {
		addSettings(
				speed = configuration.get(getModName(), "speed", 0.05, "Flight speed")
		);
	}

	@Override
	public void onDisabled() {

		// Are we still here?
		if (MC.player != null) {

			// Disable creativeflight.
			MC.player.capabilities.isFlying = false;

			// Ensure the player starts flying again.
			WRAPPER.getNetworkManager().sendPacket(new CPacketEntityAction(MC.player, Action.START_FALL_FLYING));
		}

	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {

		// Enable our flight as soon as the player starts flying his elytra.
		if (MC.player.isElytraFlying()) {
			MC.player.capabilities.isFlying = true;
		}

		MC.player.capabilities.setFlySpeed((float) speed.getDouble());

	}
}