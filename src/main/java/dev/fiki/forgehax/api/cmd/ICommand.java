package dev.fiki.forgehax.api.cmd;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import dev.fiki.forgehax.api.cmd.argument.IArgument;
import dev.fiki.forgehax.api.cmd.execution.ArgumentList;
import dev.fiki.forgehax.api.cmd.flag.EnumFlag;
import dev.fiki.forgehax.api.cmd.listener.ICommandListener;
import dev.fiki.forgehax.api.cmd.listener.IListenable;
import dev.fiki.forgehax.api.serialization.IJsonSerializable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public interface ICommand extends IListenable, IJsonSerializable {
  String getName();

  Set<String> getAliases();

  default Set<String> getNameAndAliases() {
    if (getAliases().isEmpty()) {
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
    throw new UnsupportedOperationException();
  }

  @Override
  default <T extends ICommandListener> List<T> getListeners(Class<T> type) {
    throw new UnsupportedOperationException();
  }

  @Override
  default JsonElement serialize() {
    throw new UnsupportedOperationException();
  }

  @Override
  default void deserialize(JsonElement json) {
    throw new UnsupportedOperationException();
  }

  default boolean writeConfiguration() {
    ICommand serializedNode = getSerializedNode();

    if (serializedNode == this) {
      Path config = getConfigDirectory().resolve(getFullName() + ".json");

      try {
        JsonElement root = serialize();

        CommandHelper.getWriteExecutor(this)
            .execute(() -> CommandHelper.writeConfigFile(root, config));

        return true;
      } catch (UnsupportedOperationException e) {
        getLog().warn("Unsupported serialization {}: {}", getFullName(), e.getMessage());
        getLog().debug(e, e);
      }
      return false;
    }

    // defer the config write to the serialized config node
    return serializedNode.writeConfiguration();
  }

  default boolean readConfiguration() {
    ICommand serializedNode = getSerializedNode();

    if (serializedNode == this) {
      Path config = getConfigDirectory().resolve(getFullName() + ".json");

      if (Files.exists(config)) {
        JsonElement root = CommandHelper.readConfigFile(config);

        try {
          deserialize(root);
          return true;
        } catch (UnsupportedOperationException e) {
          getLog().warn("Unsupported deserialization {}: {}", getFullName(), e.getMessage());
          getLog().debug(e, e);
        }
      }

      return false;
    }

    return serializedNode.readConfiguration();
  }

  default ICommand getSerializedNode() {
    return containsFlag(EnumFlag.SERIALIZED_NODE) ? this
        : Objects.requireNonNull(getParent(), "No serialized node found!").getSerializedNode();
  }

  default Path getConfigDirectory() {
    IParentCommand parent = getParent();
    if (parent != null) {
      try {
        return parent.getConfigDirectory();
      } catch (MissingConfigurationDirectoryException e) {
        e.add(this);
      }
    }

    throw new MissingConfigurationDirectoryException(this);
  }

  default Logger getLog() {
    return LogManager.getLogger(getClass());
  }

  class MissingConfigurationDirectoryException extends RuntimeException {
    private final List<ICommand> chain = Lists.newArrayList();

    public MissingConfigurationDirectoryException(ICommand cmd) {
      chain.add(cmd);
    }

    public void add(ICommand cmd) {
      chain.add(cmd);
    }

    @Override
    public String getMessage() {
      return "No configuration directory set for " + chain.stream()
          .map(ICommand::getName)
          .collect(Collectors.joining("."));
    }
  }
}
