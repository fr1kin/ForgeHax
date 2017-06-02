package com.matt.forgehax.mods;

import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class AntiEffectsMod extends ToggleMod {
    public Property noParticles;

    public AntiEffectsMod() {
        super("AntiPotionEffects", false, "Removes potion effects");
    }

    @Override
    public void onLoadConfiguration(Configuration configuration) {
        addSettings(
                noParticles = configuration.get(getModName(),
                        "anti_particles",
                        true,
                        "Stops the particle effect from rendering on other entities")
        );
    }

    @Override
    public void onDisabled() {
        if(MC.world != null) {
            for (Entity entity : MC.world.loadedEntityList) {
                if (entity instanceof EntityLivingBase)
                    entity.setInvisible(true);
            }
        }
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase living = event.getEntityLiving();
        if(living.equals(MC.player)) {
            living.setInvisible(false);
            living.removePotionEffect(MobEffects.NAUSEA);
            living.removePotionEffect(MobEffects.INVISIBILITY);
            living.removePotionEffect(MobEffects.BLINDNESS);
            // removes particle effect
            FastReflection.Methods.EntityLivingBase_resetPotionEffectMetadata.invoke(living);
        } else if(noParticles.getBoolean()) {
            living.setInvisible(false);
            FastReflection.Methods.EntityLivingBase_resetPotionEffectMetadata.invoke(living);
        }
    }
}
