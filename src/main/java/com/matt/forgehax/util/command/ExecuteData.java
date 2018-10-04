package com.matt.forgehax.util.command;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.matt.forgehax.Globals;
import com.matt.forgehax.util.SafeConverter;
import com.matt.forgehax.util.command.exception.CommandExecuteException;
import com.matt.forgehax.util.command.exception.MissingEntryException;
import com.matt.forgehax.util.console.ConsoleWriter;
import java.util.*;
import javax.annotation.Nullable;
import joptsimple.OptionSet;

/** Created on 6/6/2017 by fr1kin */
public class ExecuteData implements Globals, ConsoleWriter {
  public enum State {
    NONE,
    SUCCESS,
    FAILED,
    ;
  }

  private final Command command;
  private final OptionSet options;

  private final List arguments = Lists.newArrayList();

  @Nullable private Map<String, Object> data = null;

  private State state = State.FAILED;

  private boolean stopped = false;

  public ExecuteData(Command command, OptionSet options, Object[] extraArguments) {
    this.command = command;
    this.options = options;
    this.arguments.addAll(Arrays.asList(extraArguments));
    this.arguments.addAll(options.nonOptionArguments());
  }

  public State state() {
    return state;
  }

  private void setState(State state, State... conditions) {
    for (State s : conditions) if (this.state.equals(s)) return;
    this.state = state;
  }

  public void markSuccess(State... conditions) {
    setState(State.SUCCESS, conditions);
  }

  public void markFailed(State... conditions) {
    setState(State.FAILED, conditions);
  }

  public void unmark() {
    state = State.NONE;
  }

  public Command command() {
    return this.command;
  }

  public OptionSet options() {
    return this.options;
  }

  public List<?> arguments() {
    return this.arguments;
  }

  public <T> T getArgument(int index) {
    try {
      return (T) arguments.get(index);
    } catch (Throwable t) {
      return null;
    }
  }

  public String getArgumentAsString(int index) {
    return SafeConverter.toString(getArgument(index));
  }

  public int getArgumentCount() {
    return arguments.size();
  }

  public Object getOption(String name, Object defaultValue) {
    try {
      return getOptions(name).get(0);
    } catch (Throwable t) {
      return defaultValue;
    }
  }

  public Object getOption(String name) {
    return getOption(name, null);
  }

  public String getOptionAsString(String name) {
    return String.valueOf(getOption(name));
  }

  public List<?> getOptions(String name) {
    if (options.has(name))
      try {
        return options.valuesOf(name);
      } catch (Throwable t) {;
      }
    return Collections.emptyList();
  }

  public boolean hasOption(String name) {
    return options.has(name);
  }

  public <T> void set(String name, T element) {
    if (data == null) data = Maps.newHashMap();
    if (element == null) data.remove(name);
    else data.put(name, element);
  }

  public boolean has(String name) {
    return data != null && data.get(name) != null;
  }

  @SuppressWarnings("unchecked")
  public <T> T get(String name, T defaultValue) {
    try {
      Objects.requireNonNull(data);
      return (T) data.getOrDefault(name, defaultValue);
    } catch (Throwable t) {
      LOGGER.warn(String.format("Cannot find entry named \"%s\"", name));
      return defaultValue;
    }
  }

  public <T> T get(String name) {
    return get(name, null);
  }

  public void requiresEntry(String name) throws MissingEntryException {
    if (get(name) == null) {
      markFailed();
      throw new MissingEntryException(String.format("Missing data entry \"%s\"", name));
    }
  }

  public void requiredArguments(int numberRequired) {
    if (arguments.size() < numberRequired) {
      markFailed();
      throw new CommandExecuteException("Missing argument(s)");
    }
  }

  public boolean isStopped() {
    return stopped;
  }

  public void startProcessing() {
    stopped = false;
  }

  public void stopProcessing() {
    stopped = true;
  }
}
