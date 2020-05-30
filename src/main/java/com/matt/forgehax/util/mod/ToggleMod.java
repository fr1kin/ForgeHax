package com.matt.forgehax.util.mod;

import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.command.StubBuilder;
import com.matt.forgehax.util.command.callbacks.CallbackData;

public class ToggleMod extends BaseMod {
  
  private final Setting<Boolean> enabled;
  private final Setting<Boolean> hidden;
  
  public ToggleMod(Category category, String modName, boolean defaultValue, String description) {
    super(category, modName, description);
    this.enabled =
        getCommandStub()
            .builders()
            .<Boolean>newSettingBuilder()
            .name("enabled")
            .description("Enables the mod")
            .defaultTo(defaultValue)
            .changed(
                cb -> { // value is set after callback is ran 🤡
                  // do not call anything that might infinitely call this callback
                  if (cb.getTo()) {
                    start();
                  } else {
                    stop();
                  }
                })
            .build();
  }
  
  public HideMod(Category category, String modName, boolean defaultValue, String description) {
    super(category, modName, description);
    this.hidden =
        getCommandStub()
            .builders()
            .<Boolean>newSettingBuilder()
            .name("hidden")
            .description("Hides the mod from modlist")
            .defaultTo(defaultValue)
            .changed(
                cb -> { // value is set after callback is ran 🤡
                  // do not call anything that might infinitely call this callback
                  if (cb.getTo()) {
                    start();
                  } else {
                    stop();
                  }
                })
            .build();
  }
  
  /**
   * Toggle mod to be on/off
   */
  public final void toggle() {
    if (isEnabled()) {
      disable();
    } else {
      enable();
    }
  }
  
  @Override
  public void enable() {
    enabled.set(true);
  }
  
  @Override
  public void disable() {
    enabled.set(false);
  }
  
  /**
   * Toggle mod to be displayed or not
   */
  public final void toggle() {
    if (isHidden()) {
      hide();
    } else {
      show();
    }
  }
  
  @Override
  public void hide() {
    hidden.set(true);
  }
  
  @Override
  public void show() {
    hidden.set(false);
  }
  
  @Override
  protected StubBuilder buildStubCommand(StubBuilder builder) {
    return builder.kpressed(this::onBindPressed).kdown(this::onBindKeyDown).bind();
  }
  
  @Override
  public String getDebugDisplayText() {
    return super.getDebugDisplayText();
  }
 
  /**
   * Check if the mod is currently shown
   */
  
  @Override
  public boolean isHidden() {
    return hidden.get();
  }
  
  /**
   * Check if the mod is currently enabled
   */
  @Override
  public final boolean isEnabled() {
    return enabled.get();
  }
  
  @Override
  protected void onLoad() {
  }
  
  @Override
  protected void onUnload() {
  }
  
  @Override
  protected void onEnabled() {
  }
  
  @Override
  protected void onDisabled() {
  }
  
  /**
   * Toggles the mod
   */
  @Override
  public void onBindPressed(CallbackData cb) {
    toggle();
  }
  
  @Override
  protected void onBindKeyDown(CallbackData cb) {
  }
}
