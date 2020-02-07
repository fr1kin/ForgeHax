package dev.fiki.forgehax.main.util.mod;

import dev.fiki.forgehax.main.util.cmd.settings.BooleanSetting;
import lombok.AccessLevel;
import lombok.Getter;
import net.minecraft.client.settings.KeyBinding;

import java.util.Collections;

@Getter
public class ToggleMod extends KeyBoundMod {
  private final BooleanSetting enabledSetting;
  
  public ToggleMod(Category category, String modName, boolean defaultValue, String description) {
    super(category, modName, description, Collections.emptySet());
    this.enabledSetting = newBooleanSetting()
        .name("enabled")
        .description("Enables the mod")
        .defaultTo(defaultValue)
        .changedListener((from, to) -> {
          if(to) {
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
    enabledSetting.setValue(true);
  }
  
  @Override
  public void disable() {
    enabledSetting.setValue(false);
  }
  
  /**
   * Check if the mod is currently enabled
   */
  @Override
  public final boolean isEnabled() {
    return enabledSetting.getValue();
  }
  
  @Override
  protected void onLoad() { }
  
  @Override
  protected void onUnload() { }
  
  @Override
  protected void onEnabled() { }
  
  @Override
  protected void onDisabled() { }

  @Override
  public void onKeyPressed(KeyBinding key) {
    toggle();
  }

  @Override
  public void onKeyDown(KeyBinding key) { }

  @Override
  public void onKeyReleased(KeyBinding key) { }
}
