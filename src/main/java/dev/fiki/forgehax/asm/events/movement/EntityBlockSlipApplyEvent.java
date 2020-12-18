package dev.fiki.forgehax.asm.events.movement;

import dev.fiki.forgehax.api.event.Event;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;

@Getter
public class EntityBlockSlipApplyEvent extends Event {
  private final LivingEntity livingEntity;
  private final BlockState blockStateUnder;
  private final float defaultSlipperiness;

  @Setter
  private float slipperiness;

  public EntityBlockSlipApplyEvent(LivingEntity livingEntity,
      BlockState blockStateUnder, float defaultSlipperiness) {
    this.livingEntity = livingEntity;
    this.blockStateUnder = blockStateUnder;
    this.defaultSlipperiness = this.slipperiness = defaultSlipperiness;
  }
}
