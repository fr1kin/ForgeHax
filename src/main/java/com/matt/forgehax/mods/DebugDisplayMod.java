package com.matt.forgehax.mods;

import com.matt.forgehax.asm.ForgeHaxHooks;
import com.matt.forgehax.util.SurfaceUtils;
import com.matt.forgehax.util.Utils;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Map;

public class DebugDisplayMod extends ToggleMod {
    public DebugDisplayMod(String modName, boolean defaultValue, String description, int key) {
        super(modName, defaultValue, description, key);
        setHidden(true);
    }

    @SubscribeEvent
    public void onRenderScreen(RenderGameOverlayEvent.Text event) {
        int posX = event.getResolution().getScaledWidth();
        int posY = 1;
        for(Map.Entry<String,ForgeHaxHooks.DebugData> entry : ForgeHaxHooks.responding.entrySet()) {
            boolean isResponding = entry.getValue().isResponding;
            String text = String.format("%s[isResponding:%s, hasResponded:%s]",
                    entry.getKey(),
                    isResponding ? "true" : "false",
                    entry.getValue().hasResponded ? "true" : "false"
            );
            int color = isResponding ? Utils.toRGBA(0, 255, 0, 255) : Utils.toRGBA(255, 0, 0, 255);
            SurfaceUtils.drawTextShadow(text, posX - SurfaceUtils.getTextWidth(text) - 1, posY, color);
            posY += SurfaceUtils.getTextHeight() + 1;
        }
    }
}
