package com.matt.forgehax.mods;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.matt.forgehax.Globals;
import com.matt.forgehax.util.command.*;
import com.matt.forgehax.util.mod.ModProperty;
import com.matt.forgehax.util.mod.PropertyTypeConverter;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.DummyConfigElement;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.matt.forgehax.Wrapper.*;

public abstract class BaseMod implements Globals {
    // name of the mod
    private String modName;
    // description of mod
    private String modDescription;
    // category for this
    private ConfigCategory modCategory = null;

    // mod properties
    protected final List<ModProperty> properties = Lists.newArrayList();
    // mod binds
    protected final List<KeyBinding> binds = Lists.newArrayList();

    // if the mod is hidden
    private boolean isHiddenMod = false;
    // is the mod registered on the forge bus?
    private boolean registered = false;

    public BaseMod(String name, String desc) {
        // register command
        CommandRegistry.getOrCreateModRegistry(this);
        modName = name;
        modDescription = desc;
    }

    /**
     * Initialize the mod
     */
    public void initialize(Configuration configuration) {
        properties.clear();
        modCategory = configuration.getCategory(modName);
        onLoad();
    }

    /**
     * Get the categories name
     */
    public final String getModName() {
        return modName;
    }

    /**
     * Get mod description
     */
    public final String getModDescription() {
        return modDescription;
    }

    /**
     * Get mod category
     */
    public final ConfigCategory getModCategory() {
        return modCategory;
    }

    /**
     * Register event to forge bus
     */
    public final boolean register() {
        if(!registered) {
            MinecraftForge.EVENT_BUS.register(this);
            registered = true;
            return true;
        } else return false;
    }

    /**
     * Unregister event on forge bus
     */
    public final boolean unregister() {
        if(registered) {
            MinecraftForge.EVENT_BUS.unregister(this);
            registered = false;
            return true;
        } else return false;
    }

    /**
     * Check if mod is currently registered
     */
    public final boolean isRegisterd() {
        return registered;
    }

    protected final void addCommand(Command command) {
        CommandRegistry.register(this, command);
    }

    protected final void removeCommand(Command command) {
        CommandRegistry.unregister(this, command);
    }

    public final Command getCommand(String commandName) {
        return CommandRegistry.getModCommand(this, commandName);
    }

    public final Collection<Command> getCommands() {
        return Collections.unmodifiableCollection(CommandRegistry.getModRegistry(this).values());
    }

    /**
     * Add setting to list
     */
    protected final void addSettings(Property... props) {
        for(final Property prop : props) {
            properties.add(new ModProperty(prop));
            addCommand(new CommandBuilder()
                    .setProperty(prop)
                    .setProcessor(options -> {
                        List<?> args = options.nonOptionArguments();
                        if(args.size() > 0) {
                            // easier to deal with if its always a string
                            String arg = PropertyTypeConverter.getConvertedString(prop, String.valueOf(args.get(0)));
                            // save old value
                            String old = prop.getString();
                            if(!Objects.equal(arg, old)) {
                                // set
                                prop.set(arg);
                                // inform client there has been changes
                                printMessage(String.format("Set '%s' from '%s' to '%s'",
                                        CommandLine.toUniqueId(getModName(), prop.getName()),
                                        Objects.firstNonNull(old, "<null>"),
                                        Objects.firstNonNull(prop.getString(), "<null>")
                                ));
                                return true; // success
                            } else return false; // nothing changed
                        } else throw new CommandExecuteException("missing argument");
                    })
                    .addCallback(command -> {
                        update();
                        MOD.getConfig().save();
                    })
                    .build()
            );
        }
    }

    /**
     * Check if any of the settings have changed
     * if any have, return those
     */
    protected final boolean hasSettingsChanged(List<Property> changed) {
        for(ModProperty prop : properties) {
            if (prop.hasChanged()) {
                changed.add(prop.property);
                prop.update();
            }
        }
        return changed.size() > 0;
    }

    /**
     * Mods properties
     */
    public final List<ModProperty> getProperties() {
        return Collections.unmodifiableList(properties);
    }

    public final Property getProperty(String name) {
        for(ModProperty prop : properties) if(prop.property.getName().equals(name))
            return prop.property;
        return new Property("null", Boolean.toString(false), Property.Type.BOOLEAN);
    }

    /**
     * Add key bind
     */
    protected final KeyBinding addBind(String name, int keyCode) {
        KeyBinding bind = new KeyBinding(name, keyCode, "ForgeHax");
        ClientRegistry.registerKeyBinding(bind);
        binds.add(bind);
        return bind;
    }

    /**
     * Mods binds
     */
    public final Collection<KeyBinding> getKeyBinds() {
        return Collections.unmodifiableList(binds);
    }

    /**
     * Sets the mod to be hidden
     */
    protected final void setHidden(boolean b) {
        isHiddenMod = b;
    }

    /**
     * Check if the mod is hidden from the active mod list
     */
    public final boolean isHidden() {
        return isHiddenMod;
    }

    /**
     * Check if the mod is enabled
     */
    public boolean isEnabled() {
        return true;
    }

    /**
     * Toggle mod to be on/off
     */
    public void toggle() {}

    /**
     * Updates the mod
     */
    public void update() {}

    /**
     * Called when the config gui is building
     */
    public void onConfigBuildGui(List<IConfigElement> elements) {
        elements.add(new DummyConfigElement.DummyCategoryElement(
                        getModName(),
                        "",
                        new ConfigElement(getModCategory()).getChildElements())
        );
    }

    /**
     * Called when the mods settings update
     * @param changed
     */
    public void onConfigUpdated(List<Property> changed) {}

    /**
     * Register config settings
     */
    public void loadConfig(Configuration configuration) {}

    /**
     * Called when the mod is loaded
     */
    public void onLoad() {}

    /**
     * Called when the mod is enabled
     */
    public void onEnabled() {}

    /**
     * Called when the mod is disabled
     */
    public void onDisabled() {}

    /**
     * Called when the bind is initially pressed
     */
    public void onBindPressed(KeyBinding bind) {}

    /**
     * Called while the bind key is pressed down
     */
    public void onBindKeyDown(KeyBinding bind) {}

    public String getDisplayText() {
        return getModName();
    }

    @Override
    public String toString() {
        return getModName();
    }
}
