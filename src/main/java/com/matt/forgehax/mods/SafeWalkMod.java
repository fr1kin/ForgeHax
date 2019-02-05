package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;

import com.matt.forgehax.asm.ForgeHaxHooks;
import com.matt.forgehax.asm.events.AddCollisionBoxToListEvent;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Created on 9/4/2016 by fr1kin */
@RegisterMod
public class SafeWalkMod extends ToggleMod {
  public SafeWalkMod() {
    super(Category.PLAYER, "SafeWalk", false, "Prevents you from falling off blocks");
  }

  private final Setting<Boolean> collisions =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("collisions")
          .description("Give air collision boxes")
          .defaultTo(false)
          .build();
  private final Setting<Integer> min_height =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("min-height")
          .description("Minimum height above ground for collisions")
          .defaultTo(15)
          .build();

  @SubscribeEvent
  public void onAddCollisionBox(AddCollisionBoxToListEvent event) {
    if (!collisions.get()) return;

    if (getLocalPlayer() != null &&
        (EntityUtils.isDrivenByPlayer(event.getEntity()) || event.getEntity() == getLocalPlayer())) {

      AxisAlignedBB axisalignedbb = new AxisAlignedBB(event.getPos()).shrink(0.3D);
      if (event.getEntityBox().intersects(axisalignedbb)) {
        if (isAbovePlayer(event.getPos()) &&
            !hasCollisionBox(event.getPos()) &&
            !isAboveBlock(event.getPos(), min_height.get())) {

          event.getCollidingBoxes().add(axisalignedbb);
        }
      }
    }
  }

  private boolean isAbovePlayer(BlockPos pos) {
    return pos.getY() > getLocalPlayer().posY;
  }


  private boolean isAboveBlock(BlockPos pos, int minHeight) {
    for (int i = 0; i < minHeight; i++) {
      if (hasCollisionBox(pos.down(i))) return true;
    }
    return false;
  }

  private boolean hasCollisionBox(BlockPos pos) {
    return MC.world.getBlockState(pos).getCollisionBoundingBox(MC.world, pos) != null;
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
