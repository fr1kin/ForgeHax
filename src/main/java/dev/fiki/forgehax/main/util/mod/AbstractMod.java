package dev.fiki.forgehax.main.util.mod;

import dev.fiki.forgehax.main.util.cmd.ICommand;
import dev.fiki.forgehax.main.util.cmd.ISetting;
import dev.fiki.forgehax.main.util.cmd.ParentCommand;
import dev.fiki.forgehax.main.util.cmd.flag.EnumFlag;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;

import dev.fiki.forgehax.main.util.cmd.listener.IUpdateConfiguration;
import lombok.Getter;
import net.minecraftforge.common.MinecraftForge;

import static dev.fiki.forgehax.main.Common.*;
import static dev.fiki.forgehax.main.util.cmd.flag.EnumFlag.HIDDEN;
import static dev.fiki.forgehax.main.util.cmd.flag.EnumFlag.MOD_REGISTERED;

@Getter
public abstract class AbstractMod extends ParentCommand {
  // category of the mod
  private final Category category;
  
  AbstractMod(Category category, String name, String desc, Set<EnumFlag> flags) {
    super(getRootCommand(), name, Collections.emptySet(), desc, flags);
    this.category = category;
  }

  /**
   * Is the mod registered to the forge event bus
   */
  public boolean isRegistered() {
    return containsFlag(MOD_REGISTERED);
  }

  /**
   * If the mod should be visible
   */
  public boolean isHidden() {
    return containsFlag(HIDDEN);
  }

  /**
   * Check if the mod is enabled
   */
  public abstract boolean isEnabled();

  /**
   * Enable the mod
   */
  public void enable() {
    start();
  }

  /**
   * Disable the mod
   */
  public void disable() {
    stop();
  }

  /**
   * Load the mod
   */
  public final void load() {
    readConfig();
    if (isEnabled()) {
      start();
    }
    onLoad();
  }
  
  /**
   * Unload the mod
   */
  public final void unload() {
    saveConfig();
    stop();
    onUnload();
  }
  
  /**
   * Enables the mod
   */
  protected final void start() {
    if (register()) {
      onEnabled();
      getLogger().debug("{} enabled", getName());
    }
  }
  
  protected final void stop() {
    if (unregister()) {
      onDisabled();
      getLogger().debug("{} disabled", getName());
    }
  }
  
  /**
   * Register event to forge bus
   */
  public final boolean register() {
    if (!isRegistered()) {
      addFlag(MOD_REGISTERED);
      MinecraftForge.EVENT_BUS.register(this);
      return true;
    }

    return false;
  }
  
  /**
   * Unregister event on forge bus
   */
  public final boolean unregister() {
    if (isRegistered()) {
      deleteFlag(MOD_REGISTERED);
      MinecraftForge.EVENT_BUS.unregister(this);
      return true;
    }

    return false;
  }

  public void saveConfig() {
    getRootCommand().serialize(this);
  }

  public void readConfig() {
    getRootCommand().deserialize(this);
  }

  /**
   * Called when a child command updates
   * @param command child command
   */
  protected void onChildUpdateConfiguration(ICommand command) {
    saveConfig();
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
  
  public String getDisplayText() {
    return getName();
  }
  
  public String getDebugDisplayText() {
    return getDisplayText();
  }
  
  @Override
  public String toString() {
    return getName() + ": " + getDescription();
  }

  @Override
  public boolean addChild(ICommand command) {
    boolean ret = super.addChild(command);

    if(ret) {
      command.addListener(IUpdateConfiguration.class, this::onChildUpdateConfiguration);
    }

    return ret;
  }
}
