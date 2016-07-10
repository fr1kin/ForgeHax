package com.matt.forgehax.mods;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.Bindings;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AutoSprintMod extends ToggleMod {
    private boolean isBound = false;

    public AutoSprintMod(String categoryName, boolean defaultValue, String description, int key) {
        super(categoryName, defaultValue, description, key);
    }

    private void startSprinting() {
        if(!isBound) {
            Bindings.sprint.bind();
            isBound = true;
        }
        if(!Bindings.sprint.getBinding().isKeyDown())
            Bindings.sprint.setPressed(true);
    }

    private void stopSprinting() {
        if(isBound) {
            Bindings.sprint.setPressed(false);
            Bindings.sprint.unbind();
            isBound = false;
        }
    }

    /**
     * Stop sprinting when the mod is disabled
     */
    @Override
    public void onDisabled() {
        stopSprinting();
    }

    /**
     * Start sprinting every update tick
     */
    @SubscribeEvent
    public void onUpdate(LocalPlayerUpdateEvent event) {
        if(event.getEntityLiving().moveForward > 0 &&
                !event.getEntityLiving().isCollidedHorizontally &&
                !event.getEntityLiving().isSneaking()) {
            startSprinting();
        }
    }
}
