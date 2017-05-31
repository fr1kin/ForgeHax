package com.matt.forgehax.mods;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.mod.ToggleMod;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import static com.matt.forgehax.Wrapper.*;

/**
 * Created on 1/8/2017 by fr1kin
 */
public class ElytraPlus extends ToggleMod {
    public Property speed;

    public ElytraPlus() {
        super("ElytraPlus", false, "fly faster");
    }

    @Override
    public void loadConfig(Configuration configuration) {
        addSettings(
                speed = configuration.get(getModName(),
                        "speed",
                        0.05,
                        "Elytra fly speed"
                )
        );
    }

    @SubscribeEvent
    public void onUpdate(LocalPlayerUpdateEvent event) {
        if(getLocalPlayer().isElytraFlying()) {

        }
    }
}
