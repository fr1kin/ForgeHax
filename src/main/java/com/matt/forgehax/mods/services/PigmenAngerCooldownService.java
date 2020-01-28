package com.matt.forgehax.mods.services;

import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.util.mod.ServiceMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.entity.monster.ZombiePigmanEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.matt.forgehax.asm.reflection.FastReflection.Fields.EntityPigZombie_angerLevel;

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
    if (event.getEntityLiving() instanceof ZombiePigmanEntity) {
      // update pigmens anger level
      ZombiePigmanEntity pigman = (ZombiePigmanEntity) event.getEntityLiving();
      if (pigman.isAggressive()) {
        EntityPigZombie_angerLevel.set(pigman, 400);
      } else if (EntityPigZombie_angerLevel.get(pigman) > 0) {
        EntityPigZombie_angerLevel.set(pigman, EntityPigZombie_angerLevel.get(pigman) - 1);
      }
    }
  }
}
