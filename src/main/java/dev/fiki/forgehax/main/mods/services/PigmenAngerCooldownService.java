package dev.fiki.forgehax.main.mods.services;

import dev.fiki.forgehax.main.util.reflection.FastReflection;
import dev.fiki.forgehax.main.util.mod.ServiceMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraft.entity.monster.ZombiePigmanEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
        FastReflection.Fields.ZombiePigmanEntity_angerLevel.set(pigman, 400);
      } else if (FastReflection.Fields.ZombiePigmanEntity_angerLevel.get(pigman) > 0) {
        FastReflection.Fields.ZombiePigmanEntity_angerLevel.set(pigman, FastReflection.Fields.ZombiePigmanEntity_angerLevel.get(pigman) - 1);
      }
    }
  }
}
