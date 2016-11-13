package com.matt.forgehax.mods;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.key.Bindings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created on 11/12/2016 by fr1kin
 */
public class ElytraFly extends ToggleMod {
    public Property speed;

    public ElytraFly(String modName, boolean defaultValue, String description, int key) {
        super(modName, defaultValue, description, key);
    }

    @Override
    public void loadConfig(Configuration configuration) {
        addSettings(
                speed = configuration.get(getModName(),
                        "speed",
                        0.05,
                        "Flight speed"
                )
        );
    }

    @Override
    public void onDisabled() {
        if(MC.thePlayer != null) {
            MC.thePlayer.capabilities.isFlying = false;
            MC.thePlayer.motionX = MC.thePlayer.motionY = MC.thePlayer.motionZ = 0.f;
        }
    }

    @SubscribeEvent
    public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
        MC.thePlayer.capabilities.isFlying = true;
        MC.thePlayer.capabilities.setFlySpeed((float)speed.getDouble());
    }
}
