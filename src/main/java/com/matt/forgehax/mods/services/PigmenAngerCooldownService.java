package com.matt.forgehax.mods.services;

import com.google.common.eventbus.Subscribe;
import com.matt.forgehax.asm.events.replacementhooks.LivingUpdateEvent;
import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.util.mod.ServiceMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.entity.monster.EntityPigZombie;

/**
 * Created on 6/14/2017 by fr1kin
 */
@RegisterMod
public class PigmenAngerCooldownService extends ServiceMod {
    public PigmenAngerCooldownService() {
        super("PigmenAngerCooldownService");
    }

    @Subscribe
    public void onUpdate(LivingUpdateEvent event) {
        if(event.getEntity() instanceof EntityPigZombie) {
            // update pigmens anger level
            EntityPigZombie pigZombie = (EntityPigZombie)event.getEntity();
            if(pigZombie.isArmsRaised()) {
                FastReflection.Fields.EntityPigZombie_angerLevel.set(pigZombie, 400);
            } else if(pigZombie.isAngry()) {
                FastReflection.Fields.EntityPigZombie_angerLevel.set(pigZombie, FastReflection.Fields.EntityPigZombie_angerLevel.get(pigZombie) - 1);
            }
        }
    }
}
