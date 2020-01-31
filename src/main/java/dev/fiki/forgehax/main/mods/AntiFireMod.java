package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.Globals;
import dev.fiki.forgehax.main.util.command.Setting;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
    return pos.getY() >= Globals.getLocalPlayer().getPosY();
  }
  
  @SubscribeEvent
  public void onUpdate(LocalPlayerUpdateEvent event) {
    event.getEntityLiving().extinguish();
  }
}
