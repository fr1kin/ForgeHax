package dev.fiki.forgehax.main.mods.player;

import dev.fiki.forgehax.api.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.api.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.main.Common;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod(
    name = "AntiFire",
    description = "Removes fire",
    category = Category.PLAYER
)
public class AntiFireMod extends ToggleMod {

  private final BooleanSetting collisions = newBooleanSetting()
      .name("collisions")
      .description("Give fire collision boxes")
      .defaultTo(false)
      .build();

//  @SubscribeEvent
//  public void onAddCollisionBox(AddCollisionBoxToListEvent event) {
//    if (!collisions.get()) {
//      return;
//    }
//
//    if (Globals.getLocalPlayer() != null) {
//      AxisAlignedBB bb = new AxisAlignedBB(event.getPos()).expand(0, 0.1D, 0);
//      if (event.getBlock() == Blocks.FIRE && isAbovePlayer(event.getPos()) && event.getEntityBox()
//          .intersects(bb)) {
//        event.getCollidingBoxes().add(bb);
//      }
//    }
//  }
  // TODO: 1.15

  private boolean isAbovePlayer(BlockPos pos) {
    return pos.getY() >= Common.getLocalPlayer().getPosY();
  }

  @SubscribeEvent
  public void onUpdate(LocalPlayerUpdateEvent event) {
    event.getEntityLiving().extinguish();
  }
}
