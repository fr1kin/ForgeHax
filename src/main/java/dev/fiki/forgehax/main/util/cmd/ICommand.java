package dev.fiki.forgehax.main.util.cmd;

import com.google.common.collect.Sets;
import dev.fiki.forgehax.main.util.cmd.argument.IArgument;
import dev.fiki.forgehax.main.util.cmd.execution.ArgumentList;
import dev.fiki.forgehax.main.util.cmd.flag.EnumFlag;
import dev.fiki.forgehax.main.util.cmd.listener.ICommandListener;
import dev.fiki.forgehax.main.util.cmd.listener.IListenable;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public interface ICommand extends IListenable {
  String getName();
  Set<String> getAliases();

  default Set<String> getNameAndAliases() {
    if(getAliases().isEmpty()) {
      // most commands wont have aliases
      return Collections.singleton(getName());
    } else {
      Set<String> names = Sets.newHashSet();
      names.add(getName());
      names.addAll(getAliases());
      return names;
    }
  }

  String getDescription();

  default String getFullName() {
    return (getParent() == null || (getParent() instanceof RootCommand))
        ? getName()
        : getParent().getFullName() + "." + getName();
  }

  List<IArgument<?>> getArguments();

  boolean addFlag(EnumFlag flag);
  boolean deleteFlag(EnumFlag flag);

  boolean containsFlag(EnumFlag flag);

  @Nullable
  IParentCommand getParent();

  @Deprecated
  void setParent(IParentCommand command);

  ICommand onExecute(ArgumentList args);

  @Override
  default boolean addListeners(Class<? extends ICommandListener> type, Collection<? extends ICommandListener> listener) {
    return false;
  }

  @Override
  default <T extends ICommandListener> List<T> getListeners(Class<T> type) {
    return Collections.emptyList();
  }
}
