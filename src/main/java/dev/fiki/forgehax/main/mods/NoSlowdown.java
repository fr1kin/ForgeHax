package dev.fiki.forgehax.main.mods;

import com.google.common.collect.Sets;
import dev.fiki.forgehax.asm.events.BlockEntityCollisionEvent;
import dev.fiki.forgehax.asm.events.movement.PlayerSlowdownEvent;
import dev.fiki.forgehax.main.util.cmd.argument.Arguments;
import dev.fiki.forgehax.main.util.cmd.settings.collections.SimpleSettingSet;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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

  @SubscribeEvent
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

  @SubscribeEvent
  public void onBlockEntityCollision(BlockEntityCollisionEvent event) {
    if(filteredBlocks.contains(event.getBlockState().getBlock())) {
      event.setCanceled(true);
    }
  }
}
