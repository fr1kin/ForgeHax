package dev.fiki.forgehax.main.mods.player;

import com.google.common.collect.Sets;
import dev.fiki.forgehax.api.cmd.argument.Arguments;
import dev.fiki.forgehax.api.cmd.settings.collections.SimpleSettingSet;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.asm.events.movement.BlockEntityCollisionEvent;
import dev.fiki.forgehax.asm.events.movement.PlayerSlowdownEvent;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

@RegisterMod(
    name = "NoSlowDown",
    description = "Disables block slowdown",
    category = Category.PLAYER
)
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

  @SubscribeListener
  public void onPlayerSlowdown(PlayerSlowdownEvent event) {
    event.setCanceled(true);
  }

//  @SubscribeEvent
//  public void onDoApplyBlockMovement(DoBlockCollisionsEvent event) {
//    if (event.getEntity().equals(Common.getLocalPlayer())) {
//      if (Blocks.SOUL_SAND.equals(event.getState().getBlock())) { // soul sand
//        event.setCanceled(true);
//      }
//    }
//  }

  @SubscribeListener
  public void onBlockEntityCollision(BlockEntityCollisionEvent event) {
    if(filteredBlocks.contains(event.getBlockState().getBlock())) {
      event.setCanceled(true);
    }
  }
}
