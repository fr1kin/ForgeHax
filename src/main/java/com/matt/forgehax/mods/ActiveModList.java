package com.matt.forgehax.mods;

import com.matt.forgehax.mods.services.TickRateService;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.draw.SurfaceHelper;
import com.matt.forgehax.util.mod.BaseMod;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.matt.forgehax.Helper.getModManager;

@RegisterMod
public class ActiveModList extends ToggleMod {
    public final Setting<Integer> factor = getCommandStub().builders().<Integer>newSettingBuilder()
            .name("factor")
            .description("Splitting up the tick rate data")
            .defaultTo(25)
            .build();

    public ActiveModList() {
        super("ActiveMods", true, "Shows list of all active mods");
    }

    @Override
    public boolean isHidden() {
        return true;
    }

    private String generateTickRateText() {
        StringBuilder builder = new StringBuilder("Tick-rate: ");
        TickRateService.TickRateData data = TickRateService.getTickData();
        if(data.getSampleSize() <= 0) {
            builder.append("No tick data");
        } else {
            int factor = this.factor.get();
            int sections = data.getSampleSize() / factor;
            if ((sections * factor) < data.getSampleSize()) {
                TickRateService.TickRateData.CalculationData point = data.getPoint();
                builder.append(String.format("%.2f", point.getAverage()));
                builder.append(" (");
                builder.append(data.getSampleSize());
                builder.append(")");
                if (sections > 0) builder.append(", ");
            }
            if (sections > 0) {
                for (int i = sections; i > 0; i--) {
                    int at = i * factor;
                    TickRateService.TickRateData.CalculationData point = data.getPoint(at);
                    builder.append(String.format("%.2f", point.getAverage()));
                    builder.append(" (");
                    builder.append(at);
                    builder.append(")");
                    if ((i - 1) != 0) builder.append(", ");
                }
            }
        }
        return builder.toString();
    }

    @SubscribeEvent
    public void onRenderScreen(RenderGameOverlayEvent.Text event) {
        int posX = 1;
        int posY = 1;
        SurfaceHelper.drawTextShadow(generateTickRateText(), posX, posY, Utils.Colors.WHITE);
        posY += SurfaceHelper.getTextHeight() + 1;
        for(BaseMod mod : getModManager().getMods()) {
            if(mod.isEnabled() && !mod.isHidden()) {
                SurfaceHelper.drawTextShadow(">" + mod.getDisplayText(), posX, posY, Utils.Colors.WHITE);
                posY += SurfaceHelper.getTextHeight() + 1;
            }
        }
        /*
        posY += (Render2DUtils.getTextHeight() + 1) * 2;
        Render2DUtils.drawTextShadow(String.format("Pitch: %.4f", MC.thePlayer.rotationPitch), posX, posY, Utils.toRGBA(255, 255, 255, 255));
        posY += Render2DUtils.getTextHeight() + 1;
        Render2DUtils.drawTextShadow(String.format("Yaw: %.4f", MC.thePlayer.rotationYaw), posX, posY, Utils.toRGBA(255, 255, 255, 255));*/
    }
}
