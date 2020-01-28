package com.matt.forgehax.mods;

import com.matt.forgehax.asm.ForgeHaxHooks;
import com.matt.forgehax.asm.events.DoBlockCollisionsEvent;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoulSandBlock;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.matt.forgehax.Globals.*;

@RegisterMod
public class NoSlowdown extends ToggleMod {
  
  public NoSlowdown() {
    super(Category.PLAYER, "NoSlowDown", false, "Disables block slowdown");
  }
  
  @Override
  public void onEnabled() {
    ForgeHaxHooks.isNoSlowDownActivated = true;
    try {
      ForgeHaxHooks.LIST_BLOCK_FILTER.add(SoulSandBlock.class);
    } catch (Exception e) {
    }
  }
  
  @Override
  public void onDisabled() {
    ForgeHaxHooks.isNoSlowDownActivated = false;
    try {
      ForgeHaxHooks.LIST_BLOCK_FILTER.remove(SoulSandBlock.class);
    } catch (Exception e) {
    }
  }
  
  @SubscribeEvent
  public void onDoApplyBlockMovement(DoBlockCollisionsEvent event) {
    if (event.getEntity().equals(getLocalPlayer())) {
      if (Blocks.SOUL_SAND.equals(event.getState().getBlock())) { // soul sand
        event.setCanceled(true);
      }
    }
  }
}
