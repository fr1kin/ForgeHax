package com.matt.forgehax.mods;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AntiEffectsMod extends ToggleMod {
    public Property noParticles;

    public AntiEffectsMod(String modName, boolean defaultValue, String description, int key) {
        super(modName, defaultValue, description, key);
    }

    @Override
    public void loadConfig(Configuration configuration) {
        addSettings(
                noParticles = configuration.get(getModName(),
                        "anti_particles",
                        true,
                        "Stops the particle effect from rendering on other entities")
        );
    }

    @Override
    public void onDisabled() {
        if(MC.theWorld != null) {
            for (Entity entity : MC.theWorld.loadedEntityList) {
                if (entity instanceof EntityLivingBase)
                    entity.setInvisible(true);
            }
        }
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase living = event.getEntityLiving();
        if(living.equals(MC.thePlayer)) {
            living.setInvisible(false);
            living.removePotionEffect(MobEffects.NAUSEA);
            living.removePotionEffect(MobEffects.INVISIBILITY);
            living.removePotionEffect(MobEffects.BLINDNESS);
            // removes particle effect
            living.resetPotionEffectMetadata();
        } else if(noParticles.getBoolean()) {
            living.setInvisible(false);
            living.resetPotionEffectMetadata();
        }
    }
}
