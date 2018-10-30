package com.matt.forgehax.asm.events;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.eventhandler.Event;

public class EntityBlockSlipApplyEvent extends Event {
  public enum Stage {
    FIRST,
    SECOND,
    ;
  }

  private final Stage stage;
  private final EntityLivingBase entityLivingBase;
  private final IBlockState blockStateUnder;
  private final float defaultSlipperiness;
  private float slipperiness;

  public EntityBlockSlipApplyEvent(
      Stage stage,
      EntityLivingBase entityLivingBase,
      IBlockState blockStateUnder,
      float defaultSlipperiness) {
    this.stage = stage;
    this.entityLivingBase = entityLivingBase;
    this.blockStateUnder = blockStateUnder;
    this.defaultSlipperiness = defaultSlipperiness;
    this.slipperiness = defaultSlipperiness;
  }

  public Stage getStage() {
    return stage;
  }

  public EntityLivingBase getEntityLivingBase() {
    return entityLivingBase;
  }

  public IBlockState getBlockStateUnder() {
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
