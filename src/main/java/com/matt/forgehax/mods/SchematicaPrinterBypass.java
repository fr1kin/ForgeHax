package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getNetworkManager;

import com.matt.forgehax.asm.events.SchematicaPlaceBlockEvent;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.math.Angle;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

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
    Angle lookAngle = Utils.getLookAtAngles(new Vec3d(event.getVec().x + 0.5, event.getVec().y + 0.5, event.getVec().z + 0.5));
    getNetworkManager()
        .sendPacket(
            new CPacketPlayer.Rotation(
                lookAngle.getYaw(), lookAngle.getPitch(), getLocalPlayer().onGround));
    // getLocalPlayer().rotationYaw = getLocalPlayer().prevRotationYaw = (float)lookAngle.getYaw();
    // getLocalPlayer().rotationPitch = getLocalPlayer().prevRotationPitch =
    // (float)lookAngle.getPitch();
  }
}
