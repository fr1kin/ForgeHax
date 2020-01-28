package com.matt.forgehax.mods;

import com.matt.forgehax.asm.events.SchematicaPlaceBlockEvent;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.math.Angle;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.matt.forgehax.Globals.*;

/**
 * Created by Babbaj on 9/20/2017.
 */
@RegisterMod
public class SchematicaPrinterBypass extends ToggleMod {

  public SchematicaPrinterBypass() {
    super(Category.MISC, "PrinterBypass", false, "Set silent angles for schematica printer");
  }

  @SubscribeEvent
  public void onPrinterBlockPlace(SchematicaPlaceBlockEvent event) {
    final BlockPos pos = event.getPos().offset(event.getSide());
    final Vec3d vec = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

    Angle lookAngle = Utils.getLookAtAngles(vec);
    sendNetworkPacket(new CPlayerPacket.RotationPacket(lookAngle.getYaw(),
        lookAngle.getPitch(), getLocalPlayer().onGround));
  }
}
