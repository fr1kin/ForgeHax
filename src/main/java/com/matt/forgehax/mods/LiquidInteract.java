package com.matt.forgehax.mods;

import com.matt.forgehax.asm.ForgeHaxHooks;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;

@RegisterMod
public class LiquidInteract extends ToggleMod {
  
  public LiquidInteract() {
    super(Category.WORLD, "LiquidInteract", false, "Place blocks on liquids");
  }

  @Override
  public void onEnabled() {
    ForgeHaxHooks.isLiquidInteractEnabled = true;
  }
  
  @Override
  public void onDisabled() {
    ForgeHaxHooks.isLiquidInteractEnabled = false;
  }
} 
