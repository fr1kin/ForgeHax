package com.matt.forgehax.asm2;

import com.matt.forgehax.asm.events.HurtCamEffectEvent;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraftforge.common.MinecraftForge;

/**
 * Created on 1/17/2017 by fr1kin
 */
public class HookFunctions {
    public static boolean onHurtcamEffect(EntityRenderer entityRenderer, float partialTicks) {
        return MinecraftForge.EVENT_BUS.post(new HurtCamEffectEvent(partialTicks));
    }
}
