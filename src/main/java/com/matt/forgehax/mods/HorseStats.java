package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getRidingEntity;

import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Created by Babbaj on 9/1/2017. */
@RegisterMod
public class HorseStats extends ToggleMod {
  public HorseStats() {
    super(Category.PLAYER, "HorseStats", false, "Change the stats of your horse");
  }

  public final Setting<Double> jumpHeight =
      getCommandStub()
          .builders()
          .<Double>newSettingBuilder()
          .name("JumpHeight")
          .description("Modified horse jump height attribute. Default: 1")
          .defaultTo(1.0D)
          .build();
  public final Setting<Double> speed =
      getCommandStub()
          .builders()
          .<Double>newSettingBuilder()
          .name("Speed")
          .description("Modified horse speed attribute. Default: 0.3375")
          .defaultTo(0.3375D)
          .build();

  @SubscribeEvent
  public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
    if (EntityUtils.isDrivenByPlayer(event.getEntity())
        && getRidingEntity() instanceof AbstractHorse) {

      final IAttribute jump_strength =
          FastReflection.Fields.AbstractHorse_JUMP_STRENGTH.get(getRidingEntity());
      final IAttribute movement_speed =
          FastReflection.Fields.SharedMonsterAttributes_MOVEMENT_SPEED.get(getRidingEntity());

      ((EntityLivingBase) getRidingEntity())
          .getEntityAttribute(jump_strength)
          .setBaseValue(jumpHeight.getAsDouble());
      ((EntityLivingBase) getRidingEntity())
          .getEntityAttribute(movement_speed)
          .setBaseValue(speed.getAsDouble());
    }
  }
}
