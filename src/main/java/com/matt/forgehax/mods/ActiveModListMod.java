package com.matt.forgehax.mods;

import com.matt.forgehax.util.RenderUtils;
import com.matt.forgehax.util.Utils;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ActiveModListMod extends ToggleMod {
    public ActiveModListMod(String modName, boolean defaultValue, String description, int key) {
        super(modName, defaultValue, description, key);
        setHidden(true);
    }

    @SubscribeEvent
    public void onRenderScreen(RenderGameOverlayEvent.Text event) {
        int posX = 1;
        int posY = 1;
        for(BaseMod mod : MOD.mods.values()) {
            if(mod.isEnabled() && !mod.isHidden()) {
                RenderUtils.drawTextShadow(">" + mod.getModName(), posX, posY, Utils.toRGBA(255, 255, 255, 255));
                posY += RenderUtils.getTextHeight() + 1;
            }
        }
    }
}
