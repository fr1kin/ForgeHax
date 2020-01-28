package com.matt.forgehax.asm.events;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

public class EntityBlockSlipApplyEvent extends Event {
  
  public enum Stage {
    FIRST,
    SECOND,
    ;
  }
  
  private final Stage stage;
  private final LivingEntity entityLivingBase;
  private final BlockState blockStateUnder;
  private final float defaultSlipperiness;
  private float slipperiness;
  
  public EntityBlockSlipApplyEvent(Stage stage, LivingEntity entityLivingBase,
      BlockState blockStateUnder, float defaultSlipperiness) {
    this.stage = stage;
    this.entityLivingBase = entityLivingBase;
    this.blockStateUnder = blockStateUnder;
    this.defaultSlipperiness = defaultSlipperiness;
    this.slipperiness = defaultSlipperiness;
  }
  
  public Stage getStage() {
    return stage;
  }
  
  public LivingEntity getEntityLiving() {
    return entityLivingBase;
  }
  
  public BlockState getBlockStateUnder() {
    return blockStateUnder;
  }
  
  public float getDefaultSlipperiness() {
    return defaultSlipperiness;
  }
  
  public float getSlipperiness() {
    return slipperiness;
  }
  
  public void setSlipperiness(float slipperiness) {
    this.slipperiness = slipperiness;
  }
}
