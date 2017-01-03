package com.matt.forgehax.mods;

import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.key.Bindings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class FlyMod extends ToggleMod {
    private final static int VANILLA_Y_CHANGE_TIME = 2000;

    public Property speed;
    public Property vanillaBypass;

    private long lastPosResetTime = 0;

    public FlyMod(String modName, boolean defaultValue, String description, int key) {
        super(modName, defaultValue, description, key);
    }

    @Override
    public void loadConfig(Configuration configuration) {
        addSettings(
                speed = configuration.get(getModName(),
                        "speed",
                        0.05,
                        "Flight speed"
                ),
                vanillaBypass = configuration.get(getModName(),
                        "vanilla_bypass",
                        false,
                        "Bypass vanilla fly checks"
                )
        );
    }

    @Override
    public void onEnabled() {
        lastPosResetTime = System.currentTimeMillis();
    }

    @Override
    public void onDisabled() {
        lastPosResetTime = 0;
    }

    @SubscribeEvent
    public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
        EntityPlayer localPlayer = getLocalPlayer();
        MC.player.capabilities.isFlying = false;

        double speedFactor = speed.getDouble();
        localPlayer.motionX = 0;
        localPlayer.motionY = 0;
        localPlayer.motionZ = 0;
        localPlayer.jumpMovementFactor = (float)speedFactor;

        if(Bindings.jump.getBinding().isKeyDown()) {
            localPlayer.motionY += speedFactor;
        }
        if(Bindings.sneak.getBinding().isKeyDown()) {
            localPlayer.motionY -= speedFactor;
        }
    }
}
