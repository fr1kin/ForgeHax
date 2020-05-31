package com.matt.forgehax.mods;

import com.matt.forgehax.util.common.PriorityEnum;
import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.entity.mobtypes.MobType;
import com.matt.forgehax.util.entity.mobtypes.MobTypeEnum;
import com.matt.forgehax.util.entity.mobtypes.MobTypeRegistry;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.matt.forgehax.util.command.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.init.SoundEvents;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class AntiBatsMod extends ToggleMod {

  private final Setting<Boolean> bats =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("bats")
          .description("666 KILL BATS 666")
          .defaultTo(true)
          .build();

  private final Setting<Boolean> squids =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("squids")
          .description("<3 Phenom")
          .defaultTo(true)
          .build();

  private static final MobType BATS_MOBTYPE =
      new MobType() {
        @Override
        protected PriorityEnum getPriority() {
          return PriorityEnum.LOW;
        }
        
        @Override
        public boolean isMobType(Entity entity) {
          return entity instanceof EntityBat;
        }
        
        @Override
        protected MobTypeEnum getMobTypeUnchecked(Entity entity) {
          return MobTypeEnum.INVALID;
        }
      };

  private static final MobType SQUIDS_MOBTYPE =
      new MobType() {
        @Override
        protected PriorityEnum getPriority() {
          return PriorityEnum.LOW;
        }
        
        @Override
        public boolean isMobType(Entity entity) {
          return entity instanceof EntitySquid;
        }
        
        @Override
        protected MobTypeEnum getMobTypeUnchecked(Entity entity) {
          return MobTypeEnum.INVALID;
        }
      };
  
  public AntiBatsMod() {
    super(Category.WORLD, "AntiUselessMobs", false, "Disable useless passive mods");
  }
  
  @Override
  public void onEnabled() {
    MobTypeRegistry.register(BATS_MOBTYPE);
    MobTypeRegistry.register(SQUIDS_MOBTYPE);
    EntityUtils.isBatsDisabled = true;
    EntityUtils.isSquidsDisabled = true;
  }
  
  @Override
  public void onDisabled() {
    MobTypeRegistry.unregister(BATS_MOBTYPE);
    MobTypeRegistry.unregister(SQUIDS_MOBTYPE);
    EntityUtils.isBatsDisabled = false;
    EntityUtils.isSquidsDisabled = false;
  }
  
  @SubscribeEvent
  public void onRenderLiving(RenderLivingEvent.Pre<?> event) {
    if (bats.get() && event.getEntity() instanceof EntityBat) {
      event.setCanceled(true);
    }
    if (squids.get() && event.getEntity() instanceof EntitySquid) {
      event.setCanceled(true);
    }
  }
  
  @SubscribeEvent
  public void onPlaySound(PlaySoundAtEntityEvent event) {
    if (bats.get() && (
		event.getSound().equals(SoundEvents.ENTITY_BAT_AMBIENT)
        || event.getSound().equals(SoundEvents.ENTITY_BAT_DEATH)
        || event.getSound().equals(SoundEvents.ENTITY_BAT_HURT)
        || event.getSound().equals(SoundEvents.ENTITY_BAT_LOOP)
        || event.getSound().equals(SoundEvents.ENTITY_BAT_TAKEOFF))) {
      event.setVolume(0.f);
      event.setPitch(0.f);
      event.setCanceled(true);
    }
  }
}
