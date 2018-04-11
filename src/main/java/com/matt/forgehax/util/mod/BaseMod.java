package com.matt.forgehax.util.mod;

import com.matt.forgehax.Globals;
import com.matt.forgehax.util.command.Command;
import com.matt.forgehax.util.command.ExecuteData;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.command.StubBuilder;
import com.matt.forgehax.util.command.callbacks.CallbackData;
import joptsimple.internal.Strings;
import net.minecraftforge.common.MinecraftForge;

import java.util.Collection;
import java.util.Collections;

import static com.matt.forgehax.Helper.getGlobalCommand;

public abstract class BaseMod implements Globals {
    // name of the mod
    private final String modName;
    // description of mod
    private final String modDescription;
    // category of the mod
    private final Category category;

    protected final Command stubCommand;

    // is the mod registered on the forge bus?
    private boolean registered = false;

    public BaseMod(Category category, String name, String desc) {
        this.modName = name;
        this.modDescription = desc;
        this.category = category;
        stubCommand = buildStubCommand(
                getGlobalCommand().builders().newStubBuilder()
                        .name(name)
                        .description(desc)
                        .processor(this::onProcessCommand)
        ).build();
    }

    public BaseMod(Category category, String name) {
        this(category, name, Strings.EMPTY);
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
     * Get mod category
     */
    public Category getModCategory() {
        return category;
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
    public final boolean isRegistered() {
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

    private void writeChildren(StringBuilder builder, Command command, final boolean deep, final String append) {
        command.getChildren().forEach(child -> {
            boolean invalid = Strings.isNullOrEmpty(append);
            if(!invalid) {
                builder.append(append);
                builder.append(' ');
            }
            builder.append(child.getPrintText());
            builder.append('\n');
            if(deep) {
                String app = invalid ? Strings.EMPTY : append;
                writeChildren(builder, child, deep, app + ">");
            }
        });
    }

    protected void onProcessCommand(ExecuteData data) {
        if(data.getArgumentCount() == 0 && !data.options().hasOptions()) {
            final StringBuilder builder = new StringBuilder();
            writeChildren(builder, getCommandStub(), true, "");
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
