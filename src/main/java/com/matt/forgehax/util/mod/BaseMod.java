package com.matt.forgehax.util.mod;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.matt.forgehax.Globals;
import com.matt.forgehax.Wrapper;
import com.matt.forgehax.util.command.*;
import com.matt.forgehax.util.mod.property.ModProperty;
import com.matt.forgehax.util.mod.property.PropertyTypeConverter;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.DummyConfigElement;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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

    protected Command modCommand = null;

    // is the mod registered on the forge bus?
    private boolean registered = false;

    public BaseMod(String name, String desc) {
        modName = name;
        modDescription = desc;
    }

    /**
     * Load the mod
     */
    public final void load(Configuration configuration) {
        // register command first
        CommandBuilder builder = onBuildingModCommand(CommandBuilder.create());
        if(builder != null) {
            modCommand = builder.build();
            CommandRegistry.register(modCommand);
        }
        properties.clear();
        modCategory = configuration.getCategory(modName);
        loadConfig(configuration);
        onLoad();
    }

    public abstract void startup();

    /**
     * Unload the mod
     */
    public final void unload() {
        disable();
        onUnload();
        // unregister command last
        if(modCommand != null) CommandRegistry.unregister(modCommand);
    }

    /**
     * Enables the mod
     */
    public final void enable() {
        if(register()) {
            onEnabled();
            LOGGER.info(String.format("%s enabled", getModName()));
        }
    }

    public final void disable() {
        if(unregister()) {
            onDisabled();
            LOGGER.info(String.format("%s disabled", getModName()));
        }
    }

    public void loadConfig(Configuration configuration) {
        onLoadConfiguration(configuration);
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
     * The main mod command
     */
    public Command getModCommand() {
        return modCommand;
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
        java.util.Objects.requireNonNull(modCommand, "Mod base command is null");
        modCommand.addChildCommand(command);
    }

    protected final void removeCommand(Command command) {
        java.util.Objects.requireNonNull(modCommand, "Mod base command is null");
        modCommand.removeChildCommand(command);
    }

    public final Command getCommand(String commandName) {
        java.util.Objects.requireNonNull(modCommand, "Mod base command is null");
        return modCommand.getChildCommand(commandName);
    }

    public final Collection<Command> getCommands() {
        if(modCommand != null)
            return modCommand.getChildCommands();
        else
            return Collections.emptyList();
    }

    /**
     * Add setting to list
     */
    protected final void addSettings(Property... props) {
        for(final Property prop : props) {
            properties.add(new ModProperty(prop));
            addCommand(CommandBuilder.create()
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
                                return true; // success, call callbacks
                            }
                        } else {
                            printMessage(String.format("%s = %s",
                                    CommandLine.toUniqueId(getModName(), prop.getName()),
                                    Objects.firstNonNull(prop.getString(), "<null>")
                            ));
                        }
                        return false; // nothing changed, dont call callbacks
                    })
                    .addCallback(command -> {
                        update();
                        Wrapper.getConfigurationHandler().save();
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
     * Check if the mod is hidden
     * DEFAULT: true
     */
    public abstract boolean isHidden();

    /**
     * Check if the mod is enabled
     */
    public abstract boolean isEnabled();

    /**
     * Toggle mod to be on/off
     */
    public abstract void toggle();

    /**
     * Updates the mod
     */
    public abstract void update();

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
    public void onLoadConfiguration(Configuration configuration) {}

    /**
     * Called when the main mod command is being built.
     * To append to the command override this method and return super.onBuildingModCommand(builder)
     * @param builder The command builder
     * @return the command builder
     */
    @Nullable
    protected CommandBuilder onBuildingModCommand(final CommandBuilder builder) {
        return builder
                .setName(getModName())
                .setDescription(getModDescription());
    }

    /**
     * Called when the mod is loaded
     */
    protected void onLoad() {}

    /**
     * Called when unloaded
     */
    protected void onUnload() {}

    /**
     * Called when the mod is enabled
     */
    protected void onEnabled() {}

    /**
     * Called when the mod is disabled
     */
    protected void onDisabled() {}

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

    public String getDebugDisplayText() {
        return getDisplayText();
    }

    @Override
    public String toString() {
        return getModName() + ": " + getModDescription();
    }
}
