package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;

import com.matt.forgehax.asm.ForgeHaxHooks;
import com.matt.forgehax.asm.events.DoBlockCollisionsEvent;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSoulSand;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class NoSlowdown extends ToggleMod {
  
  public NoSlowdown() {
    super(Category.MOVEMENT, "NoSlowDown", false, "Disables block slowdown");
  }
  
  @Override
  public void onEnabled() {
    ForgeHaxHooks.isNoSlowDownActivated = true;
    try {
      ForgeHaxHooks.LIST_BLOCK_FILTER.add(BlockSoulSand.class);
    } catch (Exception e) {
    }
  }
  
  @Override
  public void onDisabled() {
    ForgeHaxHooks.isNoSlowDownActivated = false;
    try {
      ForgeHaxHooks.LIST_BLOCK_FILTER.remove(BlockSoulSand.class);
    } catch (Exception e) {
    }
  }
  
  @SubscribeEvent
  public void onDoApplyBlockMovement(DoBlockCollisionsEvent event) {
    if (event.getEntity().equals(getLocalPlayer())) {
      if (Block.getIdFromBlock(event.getState().getBlock()) == 88) { // soul sand
        event.setCanceled(true);
      }
    }
  }
}
