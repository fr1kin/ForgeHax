package com.matt.forgehax.mods;

import com.matt.forgehax.asm.events.SchematicaPlaceBlockEvent;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.math.Angle;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.matt.forgehax.Helper.*;

/**
 * Created by Babbaj on 9/20/2017.
 */
@RegisterMod
public class SchematicaPrinterBypass extends ToggleMod {

  public SchematicaPrinterBypass() {
    super(Category.PLAYER, "PrinterBypass", false, "Set silent angles for schematica printer");
  }

  @SubscribeEvent
  public void onPrinterBlockPlace(SchematicaPlaceBlockEvent event) {
    final BlockPos lookpos = event.getPos().offset(event.getSide());
    final Vec3d newvec = new Vec3d(lookpos.getX() + 0.5, lookpos.getY() + 0.5, lookpos.getZ() + 0.5);

    Angle lookAngle = Utils.getLookAtAngles(newvec);
    getNetworkManager()
        .sendPacket(
            new CPacketPlayer.Rotation(
                lookAngle.getYaw(), lookAngle.getPitch(), getLocalPlayer().onGround));
  }
}
