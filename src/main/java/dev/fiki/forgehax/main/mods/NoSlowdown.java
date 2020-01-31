package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.common.ForgeHaxHooks;
import dev.fiki.forgehax.common.events.movement.DoBlockCollisionsEvent;
import dev.fiki.forgehax.main.Globals;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoulSandBlock;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
    if (event.getEntity().equals(Globals.getLocalPlayer())) {
      if (Blocks.SOUL_SAND.equals(event.getState().getBlock())) { // soul sand
        event.setCanceled(true);
      }
    }
  }
}
