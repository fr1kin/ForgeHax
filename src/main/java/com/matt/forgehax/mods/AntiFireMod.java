package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;

import com.matt.forgehax.Helper;
import com.matt.forgehax.asm.events.AddCollisionBoxToListEvent;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class AntiFireMod extends ToggleMod {
  public AntiFireMod() {
    super(Category.PLAYER, "AntiFire", false, "Removes fire");
  }

  private final Setting<Boolean> collisions =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("collisions")
          .description("Give fire collision boxes")
          .defaultTo(false)
          .build();

  @SubscribeEvent
  public void onAddCollisionBox(AddCollisionBoxToListEvent event) {
    if (!collisions.get()) return;

    if (Helper.getLocalPlayer() != null) {
      AxisAlignedBB bb = new AxisAlignedBB(event.getPos()).expand(0, 0.1D, 0);
      if (event.getBlock() == Blocks.FIRE && isAbovePlayer(event.getPos()) && event.getEntityBox().intersects(bb)) {
        event.getCollidingBoxes().add(bb);
      }
    }
  }

  private boolean isAbovePlayer(BlockPos pos) {
    return pos.getY() >= getLocalPlayer().posY;
  }

  @SubscribeEvent
  public void onUpdate(LocalPlayerUpdateEvent event) {
    event.getEntityLiving().extinguish();
  }
}
