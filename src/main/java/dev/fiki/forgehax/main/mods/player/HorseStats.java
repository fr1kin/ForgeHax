package dev.fiki.forgehax.main.mods.player;

import dev.fiki.forgehax.api.cmd.settings.DoubleSetting;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.entity.LivingUpdateEvent;
import dev.fiki.forgehax.api.extension.EntityEx;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;

import static dev.fiki.forgehax.main.Common.getLocalPlayer;
import static dev.fiki.forgehax.main.Common.getMountedEntity;

@RegisterMod(
    name = "HorseStats",
    description = "Change the stats of your horse",
    category = Category.PLAYER
)
public class HorseStats extends ToggleMod {
  private final DoubleSetting jumpHeight = newDoubleSetting()
      .name("jump-height")
      .description("Modified horse jump height attribute. Default: 1")
      .defaultTo(1.0D)
      .build();

  private final DoubleSetting speed = newDoubleSetting()
      .name("speed")
      .description("Modified horse speed attribute. Default: 0.3375")
      .defaultTo(0.3375D)
      .build();

  private final DoubleSetting multiplier = newDoubleSetting()
      .name("multiplier")
      .description("multiplier while sprinting")
      .defaultTo(1.0D)
      .build();

  @Override
  public void onDisabled() {
    if (getMountedEntity() instanceof AbstractHorseEntity) {
      applyStats(jumpHeight.getDefaultValue(), speed.getDefaultValue());
    }
  }

  @SubscribeListener
  public void onLivingUpdate(LivingUpdateEvent event) {
    if (EntityEx.isDrivenByPlayer(event.getLiving())
        && getMountedEntity() instanceof AbstractHorseEntity) {

      double newSpeed = speed.getValue();
      if (getLocalPlayer().isSprinting()) {
        newSpeed *= multiplier.getValue();
      }
      applyStats(jumpHeight.getValue(), newSpeed);
    }
  }

  private void applyStats(double newJump, double newSpeed) {
    LivingEntity living = (LivingEntity) getMountedEntity();
    if (living != null) {
      living.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(newJump);
      living.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(newSpeed);
    }
  }
}
