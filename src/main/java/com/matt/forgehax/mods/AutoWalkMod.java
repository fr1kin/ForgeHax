package com.matt.forgehax.mods;

import com.matt.forgehax.events.OnLocalPlayerUpdate;
import com.matt.forgehax.util.Bindings;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AutoWalkMod extends ToggleMod {
    public Property lockView;

    private boolean isBound = false;

    public AutoWalkMod(String categoryName, boolean defaultValue, String description, int key) {
        super(categoryName, defaultValue, description, key);
    }

    @Override
    public void onDisabled() {
        super.onDisabled();
        if(isBound) {
            Bindings.forward.setPressed(false);
            Bindings.forward.unbind();
            isBound = false;
        }
    }

    @SubscribeEvent
    public void onUpdate(OnLocalPlayerUpdate event) {
        if(!isBound) {
            Bindings.forward.bind();
            isBound = true;
        }
        if(!Bindings.forward.getBinding().isKeyDown())
            Bindings.forward.setPressed(true);
    }
}
