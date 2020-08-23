package dev.fiki.forgehax.main.util.mod;

import com.google.common.base.Strings;
import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.cmd.AbstractParentCommand;
import dev.fiki.forgehax.main.util.cmd.IParentCommand;
import dev.fiki.forgehax.main.util.cmd.flag.EnumFlag;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraftforge.common.MinecraftForge;

import java.util.Collections;
import java.util.Objects;
import java.util.stream.Stream;

import static dev.fiki.forgehax.main.Common.getLogger;
import static dev.fiki.forgehax.main.Common.getRootCommand;
import static dev.fiki.forgehax.main.util.cmd.flag.EnumFlag.HIDDEN;
import static dev.fiki.forgehax.main.util.cmd.flag.EnumFlag.MOD_REGISTERED;

@Getter
public abstract class AbstractMod extends AbstractParentCommand implements Common {
  // category of the mod
  private final Category category;

  @SneakyThrows
  AbstractMod(IParentCommand parent) {
    super(parent, "invalid", Collections.emptySet(), "invalid", Collections.emptySet());

    RegisterMod info = getClass().getAnnotation(RegisterMod.class);
    Objects.requireNonNull(info, "RegisterMod annotation required for default constructor!");

    // vomit emoji
    Util.setCommandName(this, Stream.of(info.value(), info.name())
        .map(Strings::emptyToNull)
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(getClass().getSimpleName()));

    Util.setCommandDescription(this, Strings.nullToEmpty(info.description()));

    for (EnumFlag flag : info.flags()) {
      addFlag(flag);
    }

    this.category = info.category();

    addFlag(EnumFlag.SERIALIZED_NODE);

    onFullyConstructed();
  }

  AbstractMod() {
    this(getRootCommand());
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
    getLogger().debug("Loading mod {}", getName());

    readConfiguration();
    if (isEnabled()) {
      start();
    }
    onLoad();
  }

  /**
   * Unload the mod
   */
  public final void unload() {
    getLogger().debug("Unloading mod {}", getName());

    writeConfiguration();
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
}
