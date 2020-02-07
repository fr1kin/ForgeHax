package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.common.ForgeHaxHooks;
import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.main.util.cmd.settings.IntegerSetting;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraft.util.math.BlockPos;

/**
 * Created on 9/4/2016 by fr1kin
 */
@RegisterMod
public class SafeWalkMod extends ToggleMod {

  public SafeWalkMod() {
    super(Category.PLAYER, "SafeWalk", false, "Prevents you from falling off blocks");
  }

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
    return pos.getY() >= Common.getLocalPlayer().getPosY();
  }

  private boolean isAboveBlock(BlockPos pos, int minHeight) {
    for (int i = 0; i < minHeight; i++) {
      if (hasCollisionBox(pos.down(i))) {
        return true;
      }
    }
    return false;
  }

  private boolean hasCollisionBox(BlockPos pos) {
    return Common.getWorld().getBlockState(pos).getCollisionShape(Common.getWorld(), pos).isEmpty();
  }

  @Override
  public void onEnabled() {
    ForgeHaxHooks.isSafeWalkActivated = true;
  }

  @Override
  public void onDisabled() {
    ForgeHaxHooks.isSafeWalkActivated = false;
  }
}
