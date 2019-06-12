package com.matt.forgehax.mods.services;

import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.util.mod.ServiceMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.entity.monster.ZombiePigmanEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Created on 6/14/2017 by fr1kin */
@RegisterMod
public class PigmenAngerCooldownService extends ServiceMod {
  public PigmenAngerCooldownService() {
    super("PigmenAngerCooldownService");
  }

  @SubscribeEvent
  public void onUpdate(LivingEvent.LivingUpdateEvent event) {
    if (event.getEntityLiving() instanceof ZombiePigmanEntity) {
      // update pigmens anger level
      ZombiePigmanEntity pigZombie = (ZombiePigmanEntity) event.getEntityLiving();
      if (pigZombie.isArmsRaised()) {
        FastReflection.Fields.EntityPigZombie_angerLevel.set(pigZombie, 400);
      } else if (pigZombie.isAngry()) {
        FastReflection.Fields.EntityPigZombie_angerLevel.set(
            pigZombie, FastReflection.Fields.EntityPigZombie_angerLevel.get(pigZombie) - 1);
      }
    }
  }
}
