package com.matt.forgehax.util.mod;

import com.matt.forgehax.Globals;
import com.matt.forgehax.util.command.*;
import com.matt.forgehax.util.command.callbacks.CallbackData;
import joptsimple.internal.Strings;
import net.minecraftforge.common.MinecraftForge;

import java.util.Collection;
import java.util.Collections;

import static com.matt.forgehax.Helper.getGlobalCommand;

public abstract class BaseMod implements Globals {
    // name of the mod
    private String modName;
    // description of mod
    private String modDescription;

    protected final Command stubCommand;

    // is the mod registered on the forge bus?
    private boolean registered = false;

    public BaseMod(String name, String desc) {
        modName = name;
        modDescription = desc;
        stubCommand = buildStubCommand(
                getGlobalCommand().builders().newStubBuilder()
                        .name(name)
                        .description(desc)
                        .processor(this::onProcessCommand)
        ).build();
    }

    public BaseMod(String name) {
        this(name, Strings.EMPTY);
    }

    /**
     * Load the mod
     */
    public final void load() {
        if(stubCommand != null) stubCommand.deserializeAll();
        if(isEnabled()) start();
        onLoad();
    }

    /**
     * Unload the mod
     */
    public final void unload() {
        stop();
        onUnload();
        // unregister command last
        if(stubCommand != null) {
            stubCommand.serializeAll();
            stubCommand.leaveParent();
        }
    }

    /**
     * Enables the mod
     */
    protected final void start() {
        if(register()) {
            onEnabled();
            LOGGER.info(String.format("%s enabled", getModName()));
        }
    }

    protected final void stop() {
        if(unregister()) {
            onDisabled();
            LOGGER.info(String.format("%s disabled", getModName()));
        }
    }

    public void enable() {
        start();
    }

    public void disable() {
        stop();
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
     * The main mod command
     */
    public Command getCommandStub() {
        return stubCommand;
    }

    /**
     * Check if mod is currently registered
     */
    public final boolean isRegisterd() {
        return registered;
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

    protected StubBuilder buildStubCommand(StubBuilder builder) {
        return builder;
    }

    @SuppressWarnings("unchecked")
    public final <T extends Command> T getCommand(String commandName) {
        try {
            return (T) stubCommand.getChild(commandName);
        } catch (Throwable t) {
            return null;
        }
    }

    public final Setting<?> getSetting(String settingName) {
        return getCommand(settingName);
    }

    public final Collection<Command> getCommands() {
        if(stubCommand != null)
            return stubCommand.getChildren();
        else
            return Collections.emptyList();
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

    protected void onProcessCommand(ExecuteData data) {
        if(data.getArgumentCount() == 0 && !data.options().hasOptions()) {
            final StringBuilder builder = new StringBuilder();
            getCommandStub().getChildren().forEach(command -> {
                builder.append(command.getPrintText());
                builder.append('\n');
            });
            data.write(builder.toString());
        }
    }

    /**
     * Called when the mod is loaded
     */
    protected abstract void onLoad();

    /**
     * Called when unloaded
     */
    protected abstract void onUnload();

    /**
     * Called when the mod is enabled
     */
    protected abstract void onEnabled();

    /**
     * Called when the mod is disabled
     */
    protected abstract void onDisabled();

    /**
     * Called when the bind is initially pressed
     */
    protected abstract void onBindPressed(CallbackData cb);

    /**
     * Called while the bind key is pressed down
     */
    protected abstract void onBindKeyDown(CallbackData cb);

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
