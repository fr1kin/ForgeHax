package dev.fiki.forgehax.main.mods;

import com.google.common.collect.Sets;
import dev.fiki.forgehax.common.ForgeHaxHooks;
import dev.fiki.forgehax.common.events.BlockEntityCollisionEvent;
import dev.fiki.forgehax.common.events.movement.DoBlockCollisionsEvent;
import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.cmd.argument.Arguments;
import dev.fiki.forgehax.main.util.cmd.settings.collections.SimpleSettingSet;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod
public class NoSlowdown extends ToggleMod {

  private final SimpleSettingSet<Block> filteredBlocks = newSimpleSettingSet(Block.class)
      .name("filtered-blocks")
      .description("Blocks to remove collision logic on")
      .argument(Arguments.newBlockArgument()
          .label("block")
          .build())
      .supplier(Sets::newHashSet)
      .defaultsTo(Blocks.SOUL_SAND)
      .build();

  public NoSlowdown() {
    super(Category.PLAYER, "NoSlowDown", false, "Disables block slowdown");
  }

  @Override
  public void onEnabled() {
    ForgeHaxHooks.isNoSlowDownActivated = true;
  }

  @Override
  public void onDisabled() {
    ForgeHaxHooks.isNoSlowDownActivated = false;
  }

  @SubscribeEvent
  public void onDoApplyBlockMovement(DoBlockCollisionsEvent event) {
    if (event.getEntity().equals(Common.getLocalPlayer())) {
      if (Blocks.SOUL_SAND.equals(event.getState().getBlock())) { // soul sand
        event.setCanceled(true);
      }
    }
  }

  @SubscribeEvent
  public void onBlockEntityCollision(BlockEntityCollisionEvent event) {
    if(filteredBlocks.contains(event.getBlockState().getBlock())) {
      event.setCanceled(true);
    }
  }
}
