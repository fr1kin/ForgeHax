package com.matt.forgehax.mods;

import com.matt.forgehax.asm.events.HurtCamEffectEvent;
import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.asm.events.WorldCheckLightForEvent;
import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.common.PriorityEnum;
import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.entity.mobtypes.MobType;
import com.matt.forgehax.util.entity.mobtypes.MobTypeEnum;
import com.matt.forgehax.util.entity.mobtypes.MobTypeRegistry;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;


import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getWorld;

@RegisterMod
public class NoRender extends ToggleMod {

  public NoRender() {
    super(Category.RENDER, "NoRender", false, "Stops rendering things on screen");
  }

  public final Setting<Boolean> noItems =
    getCommandStub()
      .builders()
      .<Boolean>newSettingBuilder()
      .name("items")
      .description("Won't render items")
      .defaultTo(false)
      .build();

  public final Setting<Boolean> noEffects =
    getCommandStub()
      .builders()
      .<Boolean>newSettingBuilder()
      .name("effects")
      .description("Won't render effects")
      .defaultTo(true)
      .build();

  public final Setting<Boolean> noParticles =
    getCommandStub()
      .builders()
      .<Boolean>newSettingBuilder()
      .name("particles")
      .description("Won't render effect particles")
      .defaultTo(false)
      .build();

  public final Setting<Boolean> noBats =
    getCommandStub()
      .builders()
      .<Boolean>newSettingBuilder()
      .name("bats")
      .description("Won't render bats")
      .defaultTo(false)
      .build();

  public final Setting<Boolean> noSquids =
    getCommandStub()
      .builders()
      .<Boolean>newSettingBuilder()
      .name("squids")
      .description("Won't render squids")
      .defaultTo(false)
      .build();


  public final Setting<Boolean> noSkyLight =
    getCommandStub()
      .builders()
      .<Boolean>newSettingBuilder()
      .name("skylight")
      .description("Prevents skylight updates")
      .defaultTo(true)
      .build();

  public final Setting<Boolean> noHurtcam =
    getCommandStub()
      .builders()
      .<Boolean>newSettingBuilder()
      .name("hurtcam")
      .description("Won't render hurt camera shaking")
      .defaultTo(false)
      .build();

  public final Setting<Boolean> noExplosions =
    getCommandStub()
      .builders()
      .<Boolean>newSettingBuilder()
      .name("explosions")
      .description("Won't render explosion particles")
      .defaultTo(false)
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

  @Override
  public void onEnabled() {
    if (noBats.getAsBoolean()) {
      MobTypeRegistry.register(BATS_MOBTYPE);
      EntityUtils.isBatsDisabled = true;
    }

    if (noSquids.getAsBoolean()){
      MobTypeRegistry.unregister(SQUIDS_MOBTYPE);
      EntityUtils.isSquidsDisabled = true;
    }
  }

  @Override
  public void onDisabled() {
    if (noBats.getAsBoolean()) {
      MobTypeRegistry.unregister(BATS_MOBTYPE);
      EntityUtils.isBatsDisabled = false;
    }

    if (noSquids.getAsBoolean()){
      MobTypeRegistry.unregister(SQUIDS_MOBTYPE);
      EntityUtils.isSquidsDisabled = false;
    }
  }

  // AntiEffects
  @SubscribeEvent
  public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
    if (noEffects.getAsBoolean()) {
      EntityLivingBase living = event.getEntityLiving();
      if (living.equals(MC.player)) {
        living.setInvisible(false);
        living.removePotionEffect(MobEffects.NAUSEA);
        living.removePotionEffect(MobEffects.INVISIBILITY);
        living.removePotionEffect(MobEffects.BLINDNESS);

        // Removes particle effect
        FastReflection.Methods.EntityLivingBase_resetPotionEffectMetadata.invoke(living);
      } else if (noParticles.get()) {
        living.setInvisible(false);
        FastReflection.Methods.EntityLivingBase_resetPotionEffectMetadata.invoke(living);
      }
    }
  }

  @SubscribeEvent
  public void onClientTick(ClientTickEvent event) {
    if (getWorld() == null || getLocalPlayer() == null) {
      return;
    }

    // NoItems
    if(noItems.getAsBoolean()) {
      if (event.phase == TickEvent.Phase.START) {
        getWorld()
          .loadedEntityList
          .stream()
          .filter(EntityItem.class::isInstance)
          .map(EntityItem.class::cast)
          .forEach(Entity::setDead);
      }
    }
  }

  // AntiBats/Squids
  @SubscribeEvent
  public void onRenderLiving(RenderLivingEvent.Pre<?> event) {
    if (noBats.getAsBoolean() && event.getEntity() instanceof EntityBat
      || noSquids.getAsBoolean() && event.getEntity() instanceof EntitySquid) {
      event.setCanceled(true);
    }
  }

  @SubscribeEvent
  public void onPlaySound(PlaySoundAtEntityEvent event) {
    if (noBats.getAsBoolean() && event.getSound().equals(SoundEvents.ENTITY_BAT_AMBIENT)
      || event.getSound().equals(SoundEvents.ENTITY_BAT_DEATH)
      || event.getSound().equals(SoundEvents.ENTITY_BAT_HURT)
      || event.getSound().equals(SoundEvents.ENTITY_BAT_LOOP)
      || event.getSound().equals(SoundEvents.ENTITY_BAT_TAKEOFF)) {
      event.setVolume(0.f);
      event.setPitch(0.f);
      event.setCanceled(true);
    }

    if (noSquids.getAsBoolean() && event.getSound().equals(SoundEvents.ENTITY_SQUID_AMBIENT)
      || event.getSound().equals(SoundEvents.ENTITY_SQUID_DEATH)
      || event.getSound().equals(SoundEvents.ENTITY_SQUID_HURT)) {
      event.setVolume(0.f);
      event.setPitch(0.f);
      event.setCanceled(true);
    }
  }

  // NoSkyLightUpdates
  @SubscribeEvent
  public void onLightingUpdate(WorldCheckLightForEvent event) {
    if (noSkyLight.getAsBoolean() && event.getEnumSkyBlock() == EnumSkyBlock.SKY) {
      event.setCanceled(true);
    }
  }

  // AntiHurtCam
  @SubscribeEvent
  public void onHurtCamEffect(HurtCamEffectEvent event) {
    if (noHurtcam.getAsBoolean()) {
      event.setCanceled(true);
    }
  }

  // Explosions
  @SubscribeEvent
  public void onExplosionEvent(final PacketEvent.Incoming.Pre event) {
    if (noExplosions.getAsBoolean()) {
      if (!(event.getPacket() instanceof SPacketExplosion)) {
        return;
      }
      event.setCanceled(true);
    }
  }
}
