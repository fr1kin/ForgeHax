package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.util.common.PriorityEnum;
import dev.fiki.forgehax.main.util.entity.EntityUtils;
import dev.fiki.forgehax.main.util.entity.mobtypes.MobType;
import dev.fiki.forgehax.main.util.entity.mobtypes.MobTypeEnum;
import dev.fiki.forgehax.main.util.entity.mobtypes.MobTypeRegistry;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod
public class AntiBatsMod extends ToggleMod {
  
  private static final MobType BATS_MOBTYPE = new MobType() {
    @Override
    protected PriorityEnum getPriority() {
      return PriorityEnum.LOW;
    }

    @Override
    public boolean isMobType(Entity entity) {
      return entity instanceof BatEntity;
    }

    @Override
    protected MobTypeEnum getMobTypeUnchecked(Entity entity) {
      return MobTypeEnum.INVALID;
    }
  };
  
  public AntiBatsMod() {
    super(Category.RENDER, "AntiBats", false, "666 KILL BATS 666");
  }
  
  @Override
  public void onEnabled() {
    MobTypeRegistry.register(BATS_MOBTYPE);
    EntityUtils.isBatsDisabled = true;
  }
  
  @Override
  public void onDisabled() {
    MobTypeRegistry.unregister(BATS_MOBTYPE);
    EntityUtils.isBatsDisabled = false;
  }
  
  @SubscribeEvent
  public void onRenderLiving(RenderLivingEvent.Pre<? extends LivingEntity, ? extends EntityModel<?>> event) {
    if (event.getEntity() instanceof BatEntity) {
      event.setCanceled(true);
    }
  }
  
  @SubscribeEvent
  public void onPlaySound(PlaySoundAtEntityEvent event) {
    if (event.getSound().equals(SoundEvents.ENTITY_BAT_AMBIENT)
        || event.getSound().equals(SoundEvents.ENTITY_BAT_DEATH)
        || event.getSound().equals(SoundEvents.ENTITY_BAT_HURT)
        || event.getSound().equals(SoundEvents.ENTITY_BAT_LOOP)
        || event.getSound().equals(SoundEvents.ENTITY_BAT_TAKEOFF)) {
      event.setVolume(0.f);
      event.setPitch(0.f);
      event.setCanceled(true);
    }
  }
}
