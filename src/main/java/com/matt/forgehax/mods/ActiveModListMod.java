package com.matt.forgehax.mods;

import com.google.common.collect.Lists;
import com.matt.forgehax.util.TickManager;
import com.matt.forgehax.util.draw.SurfaceUtils;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

import static com.matt.forgehax.Wrapper.*;

@RegisterMod
public class ActiveModListMod extends ToggleMod {
    public Property factor;

    public ActiveModListMod() {
        super("ActiveMods", false, "Shows list of all active mods");
        setHidden(true);
    }

    private String generateTickRateText() {
        StringBuilder builder = new StringBuilder("Tick-rate: ");
        TickManager.TickRateData data = TickManager.getInstance().getData();
        int factor = this.factor.getInt();
        int sections = data.getSampleSize() / factor;
        if((sections * factor) < data.getSampleSize()) {
            TickManager.TickRateData.CalculationData point = data.getPoint();
            builder.append(String.format("%.2f", point.getAverage()));
            builder.append(" (");
            builder.append(data.getSampleSize());
            builder.append(")");
            if(sections > 0) builder.append(", ");
        }
        if(sections > 0) {
            for(int i = sections; i > 0; i--) {
                int at = i * factor;
                TickManager.TickRateData.CalculationData point = data.getPoint(at);
                builder.append(String.format("%.2f", point.getAverage()));
                builder.append(" (");
                builder.append(at);
                builder.append(")");
                if((i - 1) != 0) builder.append(", ");
            }
        }
        return builder.toString();
    }

    @Override
    public void loadConfig(Configuration configuration) {
        addSettings(
                factor = configuration.get(getModName(),
                        "factor",
                        25,
                        "Different points to show the tick rate"
                )
        );
    }

    @SubscribeEvent
    public void onRenderScreen(RenderGameOverlayEvent.Text event) {
        int posX = 1;
        int posY = 1;
        SurfaceUtils.drawTextShadow(generateTickRateText(), posX, posY, Utils.Colors.WHITE);
        posY += SurfaceUtils.getTextHeight() + 1;
        for(BaseMod mod : getModManager().getMods()) {
            if(mod.isEnabled() && !mod.isHidden()) {
                SurfaceUtils.drawTextShadow(">" + mod.getDisplayText(), posX, posY, Utils.Colors.WHITE);
                posY += SurfaceUtils.getTextHeight() + 1;
            }
        }
        /*
        posY += (Render2DUtils.getTextHeight() + 1) * 2;
        Render2DUtils.drawTextShadow(String.format("Pitch: %.4f", MC.thePlayer.rotationPitch), posX, posY, Utils.toRGBA(255, 255, 255, 255));
        posY += Render2DUtils.getTextHeight() + 1;
        Render2DUtils.drawTextShadow(String.format("Yaw: %.4f", MC.thePlayer.rotationYaw), posX, posY, Utils.toRGBA(255, 255, 255, 255));*/
    }
}
