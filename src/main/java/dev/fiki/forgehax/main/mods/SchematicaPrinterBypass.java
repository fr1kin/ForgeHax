package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;

/**
 * Created by Babbaj on 9/20/2017.
 */
@RegisterMod
public class SchematicaPrinterBypass extends ToggleMod {

  public SchematicaPrinterBypass() {
    super(Category.MISC, "PrinterBypass", false, "Set silent angles for schematica printer");
  }

//  @SubscribeEvent
//  public void onPrinterBlockPlace(SchematicaPlaceBlockEvent event) {
//    final BlockPos pos = event.getPos().offset(event.getSide());
//    final Vec3d vec = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
//
//    Angle lookAngle = Utils.getLookAtAngles(vec);
//    Globals.sendNetworkPacket(new CPlayerPacket.RotationPacket(lookAngle.getYaw(),
//        lookAngle.getPitch(), Globals.getLocalPlayer().onGround));
//  }
  // TODO: 1.15
}
