package dev.fiki.forgehax.main.mods.player;

import dev.fiki.forgehax.api.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.api.cmd.settings.IntegerSetting;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.asm.events.movement.ClipBlockEdgeEvent;
import dev.fiki.forgehax.main.Common;
import net.minecraft.util.math.BlockPos;

@RegisterMod(
    name = "SafeWalk",
    description = "Prevents you from falling off blocks",
    category = Category.PLAYER
)
public class SafeWalkMod extends ToggleMod {

  private final BooleanSetting collisions = newBooleanSetting()
      .name("collisions")
      .description("Give air collision boxes")
      .defaultTo(false)
      .build();

  private final IntegerSetting min_height = newIntegerSetting()
      .name("min-height")
      .description("Minimum height above ground for collisions")
      .defaultTo(15)
      .build();

//  @SubscribeEvent
//  public void onAddCollisionBox(AddCollisionBoxToListEvent event) {
//    if (!collisions.get()) {
//      return;
//    }
//
//    if (Globals.getLocalPlayer() != null &&
//        (EntityUtils.isDrivenByPlayer(event.getEntity())
//            || event.getEntity() == Globals.getLocalPlayer())) {
//      AxisAlignedBB axisalignedbb = new AxisAlignedBB(event.getPos()).shrink(0.3D);
//      if (event.getEntityBox().intersects(axisalignedbb)) {
//        if (isAbovePlayer(event.getPos()) &&
//            !hasCollisionBox(event.getPos()) &&
//            !isAboveBlock(event.getPos(), min_height.get())) {
//
//          event.getCollidingBoxes().add(axisalignedbb);
//        }
//      }
//    }
//  }
  // TODO: 1.15

  private boolean isAbovePlayer(BlockPos pos) {
    return pos.getY() >= Common.getLocalPlayer().getY();
  }

  private boolean isAboveBlock(BlockPos pos, int minHeight) {
    for (int i = 0; i < minHeight; i++) {
      if (hasCollisionBox(pos.below(i))) {
        return true;
      }
    }
    return false;
  }

  private boolean hasCollisionBox(BlockPos pos) {
    return Common.getWorld().getBlockState(pos).getCollisionShape(Common.getWorld(), pos).isEmpty();
  }

  @SubscribeListener
  public void onClipBlockEdge(ClipBlockEdgeEvent event) {
    event.setCanceled(true);
  }
}
