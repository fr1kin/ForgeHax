package com.matt.forgehax.mods;

import com.matt.forgehax.mods.services.TickRateService;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.draw.SurfaceHelper;
import com.matt.forgehax.util.mod.BaseMod;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.matt.forgehax.Helper.getModManager;

@RegisterMod
public class ActiveModList extends ToggleMod {
    public final Setting<Boolean> tps_meter = getCommandStub().builders().<Boolean>newSettingBuilder()
            .name("tps-meter")
            .description("Shows the server tps")
            .defaultTo(true)
            .build();

    public final Setting<Boolean> debug = getCommandStub().builders().<Boolean>newSettingBuilder()
            .name("debug")
            .description("Disables debug text on mods that have it")
            .defaultTo(false)
            .build();

    public final Setting<Integer> factor = getCommandStub().builders().<Integer>newSettingBuilder()
            .name("factor")
            .description("Splitting up the tick rate data")
            .defaultTo(25)
            .min(1)
            .max(100)
            .build();

    public ActiveModList() {
        super(Category.RENDER, "ActiveMods", true, "Shows list of all active mods");
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
        if(tps_meter.get()) {
            SurfaceHelper.drawTextShadow(generateTickRateText(), posX, posY, Utils.Colors.WHITE);
            posY += SurfaceHelper.getTextHeight() + 1;
        }
        for(BaseMod mod : getModManager().getMods()) {
            if(mod.isEnabled() && !mod.isHidden()) {
                SurfaceHelper.drawTextShadow(">" + (debug.get() ? mod.getDebugDisplayText() : mod.getDisplayText()), posX, posY, Utils.Colors.WHITE);
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
