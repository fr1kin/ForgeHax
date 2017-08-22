package com.matt.forgehax.mods;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.SimpleTimer;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.matt.forgehax.Helper.getLocalPlayer;

@RegisterMod
public class AntiAfkMod extends ToggleMod {
    public final Setting<Long> delay = getCommandStub().builders().<Long>newSettingBuilder()
            .name("delay")
            .description("Delay between swings in ms")
            .defaultTo(5000L)
            .build();

    public SimpleTimer timer = new SimpleTimer();

    public AntiAfkMod() {
        super("AntiAFK", false, "Swing arm to prevent being afk kicked");
    }

    @SubscribeEvent
    public void onUpdate(LocalPlayerUpdateEvent event) {
        if(!timer.isStarted())
            timer.start();
        else {
            if(timer.hasTimeElapsed(delay.get())) {
                timer.start(); // restart timer
                getLocalPlayer().swingArm(EnumHand.MAIN_HAND);
            }
        }
    }
}
