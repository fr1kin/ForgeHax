package com.matt.forgehax.mods.services;

import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.util.mod.ServiceMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created on 6/14/2017 by fr1kin
 */
@RegisterMod
public class PigmenAngerCooldownService extends ServiceMod {
    public PigmenAngerCooldownService() {
        super("PigmenAngerCooldownService");
    }

    @SubscribeEvent
    public void onUpdate(LivingEvent.LivingUpdateEvent event) {
        if(event.getEntityLiving() instanceof EntityPigZombie) {
            // update pigmens anger level
            if(((EntityPigZombie) event.getEntityLiving()).isAngry())
                FastReflection.Fields.EntityPigZombie_angerLevel.set(event.getEntity(), FastReflection.Fields.EntityPigZombie_angerLevel.get(event.getEntity()) - 1);
        }
    }
}
