package com.matt.forgehax.mods;

import com.matt.forgehax.Helper;
import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class HorseJump extends ToggleMod {
  
  public HorseJump() {
    super(Category.PLAYER, "HorseJump", false, "always max horse jump");
  }
  
  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    FastReflection.Fields.EntityPlayerSP_horseJumpPower.set(Helper.getLocalPlayer(), 1.F);
  }
}
