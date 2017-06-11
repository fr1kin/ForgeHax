package com.matt.forgehax.mods;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.matt.forgehax.Wrapper.getLocalPlayer;

/**
 * Created by BabbaJ
 */

@RegisterMod
public class AutoPvPLog extends ToggleMod {
    public final Setting<Integer> threshold = getCommandStub().builders().<Integer>newSettingBuilder()
            .name("threshold")
            .description("Health to disconnect at")
            .defaultTo(0)
            .build();

    public AutoPvPLog () {
        super("AutoPvPLog",false,"automatically disconnect");
    }

    @SubscribeEvent
    public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
        if (getLocalPlayer().getHealth() <= threshold.get()) {
            MC.player.sendChatMessage("");
        }
    }
}
