package dev.fiki.forgehax.api.mod;

import dev.fiki.forgehax.api.cmd.flag.EnumFlag;
import dev.fiki.forgehax.api.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import lombok.Getter;
import net.minecraft.client.settings.KeyBinding;

@Getter
public class ToggleMod extends KeyBoundMod {
  private final BooleanSetting enabledSetting;

  {
    newSimpleCommand()
        .name("toggle")
        .description("Toggles the mods enable state ")
        .executor(args -> getEnabledSetting().setValue(!getEnabledSetting().getValue()))
        .build();
  }

  public ToggleMod() {
    addFlag(EnumFlag.TOGGLE_MOD);

    this.enabledSetting = newBooleanSetting()
        .name("enabled")
        .description("Enables the mod")
        .defaultTo(getClass().getAnnotation(RegisterMod.class).enabled())
        .changedListener((from, to) -> {
          if(to) {
            start();
          } else {
            stop();
          }
        })
        .build();
  }

  @Override
  protected void onFullyConstructed() {
    super.onFullyConstructed();
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
  public final void enable() {
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
