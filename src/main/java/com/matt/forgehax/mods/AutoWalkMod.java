package com.matt.forgehax.mods;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.key.Bindings;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AutoWalkMod extends ToggleMod {
    public Property stopAtUnloadedChunks;

    private boolean isBound = false;

    public AutoWalkMod(String categoryName, boolean defaultValue, String description, int key) {
        super(categoryName, defaultValue, description, key);
    }

    @Override
    public void loadConfig(Configuration configuration) {
        addSettings(
                stopAtUnloadedChunks = configuration.get(getModName(),
                        "stop_at_unloaded_chunks",
                        true,
                        "Stop moving at unloaded chunks")
        );
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
    public void onUpdate(LocalPlayerUpdateEvent event) {
        if(!isBound) {
            Bindings.forward.bind();
            isBound = true;
        }
        if(!Bindings.forward.getBinding().isKeyDown())
            Bindings.forward.setPressed(true);

        if(stopAtUnloadedChunks.getBoolean()) {
            if(!WRAPPER.getWorld().getChunkFromBlockCoords(WRAPPER.getLocalPlayer().getPosition()).isLoaded())
                Bindings.forward.setPressed(false);
        }
    }
}
