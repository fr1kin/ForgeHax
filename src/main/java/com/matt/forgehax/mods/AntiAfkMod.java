package com.matt.forgehax.mods;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.SimpleTimer;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getNetworkManager;

@RegisterMod
public class AntiAfkMod extends ToggleMod {
    public final Setting<Long> delay = getCommandStub().builders().<Long>newSettingBuilder()
            .name("delay")
            .description("Delay between swings in ms")
            .defaultTo(5000L)
            .build();
    public final Setting<Boolean> silent = getCommandStub().builders().<Boolean>newSettingBuilder()
            .name("silent")
            .description("silently swing arm")
            .defaultTo(false)
            .build();

    public SimpleTimer timer = new SimpleTimer();

    public AntiAfkMod() {
        super("AntiAFK", false, "Swing arm to prevent being afk kicked");
    }

    // Reset the timer if the player is not afk
    @SubscribeEvent
    public void onKeyboardinput(InputEvent.KeyInputEvent event) {
        if(timer.isStarted())
            timer.start();
    }

    @SubscribeEvent
    public void onUpdate(LocalPlayerUpdateEvent event) {
        if(!timer.isStarted())
            timer.start();
        else {
            if(timer.hasTimeElapsed(delay.get())) {
                timer.start(); // restart timer
                if (silent.getAsBoolean())
                    getNetworkManager().sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
                else
                    getLocalPlayer().swingArm(EnumHand.MAIN_HAND);
            }
        }
    }
}
