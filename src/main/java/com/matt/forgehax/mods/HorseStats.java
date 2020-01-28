package com.matt.forgehax.mods;

import com.matt.forgehax.Globals;
import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.matt.forgehax.Globals.*;

/**
 * Created by Babbaj on 9/1/2017.
 */
@RegisterMod
public class HorseStats extends ToggleMod {
  
  public HorseStats() {
    super(Category.PLAYER, "HorseStats", false, "Change the stats of your horse");
  }
  
  private final Setting<Double> jumpHeight =
      getCommandStub()
          .builders()
          .<Double>newSettingBuilder()
          .name("JumpHeight")
          .description("Modified horse jump height attribute. Default: 1")
          .defaultTo(1.0D)
          .build();
  private final Setting<Double> speed =
      getCommandStub()
          .builders()
          .<Double>newSettingBuilder()
          .name("Speed")
          .description("Modified horse speed attribute. Default: 0.3375")
          .defaultTo(0.3375D)
          .build();
  
  private final Setting<Double> multiplier =
      getCommandStub()
          .builders()
          .<Double>newSettingBuilder()
          .name("multiplier")
          .description("multiplier while sprinting")
          .defaultTo(1.0D)
          .build();
  
  @Override
  public void onDisabled() {
    if (getMountedEntity() instanceof AbstractHorseEntity) {
      applyStats(jumpHeight.getDefault(), speed.getDefault());
    }
  }
  
  @SubscribeEvent
  public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
    if (EntityUtils.isDrivenByPlayer(event.getEntity())
        && getMountedEntity() instanceof AbstractHorseEntity) {
      
      double newSpeed = speed.getAsDouble();
      if (getLocalPlayer().isSprinting()) {
        newSpeed *= multiplier.getAsDouble();
      }
      applyStats(jumpHeight.getAsDouble(), newSpeed);
    }
  }
  
  private void applyStats(double newJump, double newSpeed) {
    final IAttribute jump_strength =
        FastReflection.Fields.AbstractHorse_JUMP_STRENGTH.get(getMountedEntity());
    final IAttribute movement_speed =
        FastReflection.Fields.SharedMonsterAttributes_MOVEMENT_SPEED.get(getMountedEntity());
    
    ((LivingEntity) getMountedEntity())
        .getAttribute(jump_strength)
        .setBaseValue(newJump);
    ((LivingEntity) getMountedEntity())
        .getAttribute(movement_speed)
        .setBaseValue(newSpeed);
  }
}
