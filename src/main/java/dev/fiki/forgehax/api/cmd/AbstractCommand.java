package dev.fiki.forgehax.api.cmd;

import com.google.common.collect.ImmutableSet;
import dev.fiki.forgehax.api.cmd.flag.EnumFlag;
import dev.fiki.forgehax.api.cmd.listener.ICommandListener;
import dev.fiki.forgehax.api.cmd.listener.IOnUpdate;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.*;
import java.util.function.Consumer;

@Getter
public abstract class AbstractCommand implements ICommand {
  @Setter
  private IParentCommand parent;

  private String name;
  private String description;

  private final Set<String> aliases;
  private final Set<EnumFlag> flags;

  public AbstractCommand(IParentCommand parent,
      @NonNull String name, @NonNull Collection<String> aliases, @NonNull String description,
      @NonNull Collection<EnumFlag> flags) {
    this.name = name;
    this.aliases = ImmutableSet.copyOf(aliases);
    this.description = description;

    EnumSet<EnumFlag> es = EnumSet.noneOf(EnumFlag.class);
    es.addAll(flags);
    this.flags = Collections.synchronizedSet(es);

    // set the parent, but don't add it until this object is fully constructed
    this.parent = parent;
  }

  /**
   * This method should be called when the top most constructor has finished.
   */
  protected void onFullyConstructed() {
    if(parent != null) {
      // all this command to the parent
      parent.addChild(this);
    }
  }

  protected <T extends ICommandListener> void invokeListeners(Class<T> type, Consumer<T> call) {
    CommandHelper.getExecutor(this)
        .execute(() -> getListeners(type).forEach(call));
  }

  protected void callUpdateListeners() {
    invokeListeners(IOnUpdate.class, l -> l.onUpdate(this));
    writeConfiguration();
  }

  @Override
  public boolean addFlag(EnumFlag flag) {
    return flags.add(flag);
  }

  @Override
  public boolean deleteFlag(EnumFlag flag) {
    return flags.remove(flag);
  }

  @Override
  public boolean containsFlag(EnumFlag flag) {
    return flags.contains(flag);
  }

  @Override
  public String toString() {
    return getName();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AbstractCommand command = (AbstractCommand) o;
    return name.equalsIgnoreCase(command.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name.toLowerCase());
  }
}
