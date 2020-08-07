package dev.fiki.forgehax.main.mods.services;

import dev.fiki.forgehax.main.util.mod.ServiceMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import net.minecraft.entity.monster.ZombifiedPiglinEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod
public class PigmenAngerCooldownService extends ServiceMod {
  @SubscribeEvent
  public void onUpdate(LivingEvent.LivingUpdateEvent event) {
    if (event.getEntityLiving() instanceof ZombifiedPiglinEntity) {
      // update pigmens anger level
      ZombifiedPiglinEntity pigman = (ZombifiedPiglinEntity) event.getEntityLiving();
      if (pigman.isAggressive()) {
        pigman.func_230260_a__(400);
      } else if (pigman.func_230256_F__() > 0) {
        pigman.func_230260_a__(pigman.func_230256_F__() - 1);
      }
    }
  }
}
