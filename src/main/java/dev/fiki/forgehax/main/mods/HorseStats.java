package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.reflection.FastReflection;
import dev.fiki.forgehax.main.util.command.Setting;
import dev.fiki.forgehax.main.util.entity.EntityUtils;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
    if (Common.getMountedEntity() instanceof AbstractHorseEntity) {
      applyStats(jumpHeight.getDefault(), speed.getDefault());
    }
  }
  
  @SubscribeEvent
  public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
    if (EntityUtils.isDrivenByPlayer(event.getEntity())
        && Common.getMountedEntity() instanceof AbstractHorseEntity) {
      
      double newSpeed = speed.getAsDouble();
      if (Common.getLocalPlayer().isSprinting()) {
        newSpeed *= multiplier.getAsDouble();
      }
      applyStats(jumpHeight.getAsDouble(), newSpeed);
    }
  }
  
  private void applyStats(double newJump, double newSpeed) {
    final IAttribute jump_strength =
        FastReflection.Fields.AbstractHorse_JUMP_STRENGTH.get(Common.getMountedEntity());
    final IAttribute movement_speed =
        FastReflection.Fields.SharedMonsterAttributes_MOVEMENT_SPEED.get(Common.getMountedEntity());
    
    ((LivingEntity) Common.getMountedEntity())
        .getAttribute(jump_strength)
        .setBaseValue(newJump);
    ((LivingEntity) Common.getMountedEntity())
        .getAttribute(movement_speed)
        .setBaseValue(newSpeed);
  }
}
