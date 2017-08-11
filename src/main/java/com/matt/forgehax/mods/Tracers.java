package com.matt.forgehax.mods;

import com.matt.forgehax.util.draw.SurfaceUtils;
import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.math.VectorUtils;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Objects;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getWorld;

/**
 * Created on 8/6/2017 by fr1kin
 */
@RegisterMod
public class Tracers extends ToggleMod {
    public Tracers() {
        super("Tracers", false, "See where other players are");
    }

    @SubscribeEvent
    public void onDrawScreen(RenderGameOverlayEvent.Text event) {
        final double dX = (double) MC.displayWidth / 4.D;
        final double dY = (double) MC.displayHeight / 4.D;
        getWorld().loadedEntityList.stream()
                .filter(entity -> !Objects.equals(entity, getLocalPlayer()))
                .filter(entity -> entity instanceof AbstractClientPlayer)
                .forEach(entity -> {
                    Vec3d pos3d = EntityUtils.getInterpolatedEyePos(entity, MC.getRenderPartialTicks());
                    VectorUtils.ScreenPos pos = VectorUtils.toScreen(pos3d);
                    if(!pos.isVisible) {
                        double cx = pos.x, cy = pos.y;

                        double x = (cx - dX) / dX;
                        double y = (cy - dY) / dY;

                        double mag = Math.abs(Math.sqrt(x*x + y*y));
                        double normX = x / mag;
                        double normY = y / mag;

                        double prodX = dX + normX * dX;
                        double prodY = dY + normY * dY;

                        ResourceLocation resourceLocation = AbstractClientPlayer.getLocationSkin(entity.getName());
                        AbstractClientPlayer.getDownloadImageSkin(resourceLocation, entity.getName());

                        SurfaceUtils.drawHead(resourceLocation, (int)prodX, (int)prodY, 1);
                    }
                });
    }
}
