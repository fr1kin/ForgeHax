package com.matt.forgehax.mods;

import com.matt.forgehax.asm.events.SchematicaPlaceBlockEvent;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.math.Angle;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.matt.forgehax.Helper.*;

/**
 * Created by Babbaj on 9/20/2017.
 */
@RegisterMod
public class SchematicaPrinterBypass extends ToggleMod {

    public SchematicaPrinterBypass() { super(Category.NONE, "PrinterBypass", false, "Set silent angles for schematica printer"); }

    @Override
    public boolean isHidden() {
        return true;
    }

    @SubscribeEvent
    public void onPrinterBlockPlace(SchematicaPlaceBlockEvent event) {
        Angle lookAngle = Utils.getLookAtAngles(event.getVec());
        getNetworkManager().sendPacket(new CPacketPlayer.Rotation((float)lookAngle.getYaw(), (float)lookAngle.getPitch(), getLocalPlayer().onGround));
        //getLocalPlayer().rotationYaw = getLocalPlayer().prevRotationYaw = (float)lookAngle.getYaw();
        //getLocalPlayer().rotationPitch = getLocalPlayer().prevRotationPitch = (float)lookAngle.getPitch();
    }

}
