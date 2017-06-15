package com.matt.forgehax.util.mod;

import com.matt.forgehax.util.command.CommandBuilder;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.command.callbacks.CallbackData;

public class ToggleMod extends BaseMod {
    private final Setting<Boolean> enabled;

    public ToggleMod(String modName, boolean defaultValue, String description) {
        super(modName, description);
        this.enabled = getCommandStub().builders().<Boolean>newSettingBuilder()
                .name("enabled")
                .description("Enables the mod")
                .defaultTo(defaultValue)
                .changed(cb -> {
                    // do not call anything that might infinitely call this callback
                    if(cb.getTo())
                        start();
                    else
                        stop();

                    Setting<Boolean> setting = cb.command();
                    cb.write(String.format("%s changed to \"%s\"",
                            setting.getAbsoluteName(),
                            setting.getConverter().toString(cb.getTo())
                    ));
                })
                .build();
    }

    /**
     * Toggle mod to be on/off
     */
    public final void toggle() {
        if(isEnabled())
            enable();
        else
            disable();
    }

    @Override
    public void enable() {
        enabled.set(true);
    }

    @Override
    public void disable() {
        enabled.set(false);
    }

    @Override
    protected CommandBuilder buildStubCommand(CommandBuilder builder) {
        return builder
                .kpressed(this::onBindPressed)
                .kdown(this::onBindKeyDown)
                .bind()
                ;
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    /**
     * Check if the mod is currently enabled
     */
    @Override
    public final boolean isEnabled() {
        return enabled.get();
    }

    @Override
    protected void onLoad() {}

    @Override
    protected void onUnload() {}

    @Override
    protected void onEnabled() {}

    @Override
    protected void onDisabled() {}

    /**
     * Toggles the mod
     */
    @Override
    public void onBindPressed(CallbackData cb) {
        toggle();
    }

    @Override
    protected void onBindKeyDown(CallbackData cb) {}
}
