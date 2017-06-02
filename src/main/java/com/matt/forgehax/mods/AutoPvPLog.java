package com.matt.forgehax.mods;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraftforge.common.config.Configuration;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class AutoPvPLog extends ToggleMod {

    public AutoPvPLog () {
        super("AutoPvPLog",false,"automatically disconnect");
    }
    public Property threshold;

    @Override
    public void onLoadConfiguration(Configuration configuration) {
        addSettings( threshold = configuration.get(getModName(), "threshold", 0, "meme font") );
    }

    @SubscribeEvent
    public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
        if (MC.player.getHealth() <= threshold.getInt())
            MC.player.sendChatMessage("");
    }
}
