package com.matt.forgehax.mods;

import com.google.common.collect.Lists;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import org.lwjgl.input.Keyboard;

import java.util.List;

@RegisterMod
public abstract class ToggleMod extends BaseMod {
    // setting every mod should have to enable/disable it
    private Property enabled = null;
    // toggle key bind
    private KeyBinding toggleBind = null;

    // default on/off
    private boolean defaultValue;

    public ToggleMod(String modName, boolean defaultValue, String description, int key) {
        super(modName, description);
        this.defaultValue = defaultValue;
        if(key != -1)
            toggleBind = addBind(modName, key);
    }

    public ToggleMod(String modName, boolean defaultValue, String description) {
        this(modName, defaultValue, description, Keyboard.KEY_NONE);
    }

    /**
     * Initializes the configurations for this mod
     */
    public final void initialize(Configuration configuration) {
        super.initialize(configuration);
        addSettings(enabled = configuration.get(getModName(), "enabled", defaultValue, getModDescription()));
        loadConfig(configuration);
    }

    /**
     * Toggle mod to be on/off
     */
    public final void toggle() {
        // toggles mod
        enabled.set(!enabled.getBoolean());
        // call config changed method
        update();
        // saves config
        MOD.getConfig().save();
    }

    /**
     * Check if the mod is currently enabled
     */
    public final boolean isEnabled() {
        return enabled.getBoolean();
    }

    /**
     * Called when the config is changed
     */
    public final void update() {
        List<Property> changed = Lists.newArrayList();
        if(hasSettingsChanged(changed)) {
            if (enabled.getBoolean()) {
                if (register()) {
                    onEnabled();
                    MOD.getLog().info(String.format("%s enabled", getModName()));
                }
            } else {
                if (unregister()) {
                    onDisabled();
                    MOD.getLog().info(String.format("%s disabled", getModName()));
                }
            }
            onConfigUpdated(changed);
        }
    }

    /**
     * Toggles the mod
     */
    @Override
    public void onBindPressed(KeyBinding bind) {
        if(bind.equals(toggleBind))
            toggle();
    }
}
