package com.matt.forgehax.util.command.v2;

import com.google.common.collect.ImmutableList;
import com.matt.forgehax.util.command.v2.argument.IArg;
import com.matt.forgehax.util.command.v2.argument.IOption;
import com.matt.forgehax.util.command.v2.callback.ICmdCallback;
import com.matt.forgehax.util.command.v2.exception.CmdAmbiguousException;
import com.matt.forgehax.util.command.v2.exception.CmdMissingArgumentException;
import com.matt.forgehax.util.command.v2.exception.CmdUnknownException;
import com.matt.forgehax.util.command.v2.flag.ICmdFlag;
import com.matt.forgehax.util.serialization.ISerializableJson;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Created on 12/25/2017 by fr1kin */

/*
   Features:
       Name                Immutable       Name of the command
       Description         Immutable       Description of the command
       Aliases             Immutable       Alternative names of the command
       Parent              Immutable       Parent of the command
       Arguments           Immutable       Command arguments (if any)
       Options             Immutable       Command options (if any)
       Flags               Mutable         Command flags (if any)
       Callbacks           Mutable         Command callback
*/
public interface ICmd extends ISerializableJson, Comparable<ICmd> {
  /**
   * The formal name for this command
   *
   * @return formal name
   */
  String getName();

  String getAbsoluteName();

  /**
   * Alternative names for this command.
   *
   * @return list of alternative names or an empty array list if none exist.
   */
  @Nonnull
  Collection<String> getAliases();

  /**
   * Returns names and aliases
   *
   * @return list of names and aliases
   */
  default Collection<String> getAllNames() {
    return getAliases().isEmpty()
        ? Collections.singleton(getName())
        : ImmutableList.<String>builder().add(getName()).addAll(getAliases()).build();
  }

  /**
   * Check if the string matches this commands name
   *
   * @param name
   * @return
   */
  default boolean isNameMatching(String name) {
    return getName().equalsIgnoreCase(name);
  }

  default boolean isNameMatching(ICmd other) {
    return isNameMatching(other.getName());
  }

  default boolean isAbsoluteNameMatching(String name) {
    return getAbsoluteName().equalsIgnoreCase(name);
  }

  default boolean isAbsoluteNameMatching(ICmd other) {
    return isAbsoluteNameMatching(other.getAbsoluteName());
  }

  /**
   * If this command can be identified by the given name. Command names are case insensitive.
   *
   * @param name
   * @return
   */
  default boolean isIdentifiableAs(final String name) {
    return CmdHelper.isNameValid(name)
        && (isNameMatching(name)
            || isAbsoluteNameMatching(name)
            || getAliases().stream().anyMatch(alias -> alias.equalsIgnoreCase(name)));
  }

  /**
   * Check if these two commands have conflicting naming schemes
   *
   * @param other
   * @return
   */
  default boolean isConflictingWith(ICmd other) {
    return isNameMatching(other.getName());
  }

  /**
   * Commands description
   *
   * @return description
   */
  String getDescription();

  /**
   * The parent of this command
   *
   * @return null if no parent
   */
  @Nullable
  IParentCmd getParent();

  List<IArg<?>> getArguments();

  default IArg<?> getArgument(int index, IArg<?> defaultTo) {
    return (index > -1 && index < getArgumentCount()) ? getArguments().get(index) : defaultTo;
  }

  default IArg<?> getArgument(int index) {
    return getArgument(index, null);
  }

  default int getArgumentCount() {
    return getArguments().size();
  }

  default int getRequiredArgumentCount() {
    return (int) getArguments().stream().filter(IArg::isRequired).count();
  }

  default int getOptionalArgumentCount() {
    return (int) getArguments().stream().filter(arg -> !arg.isRequired()).count();
  }

  List<IOption<?>> getOptions();

  default IOption<?> getOption(String name, IOption<?> defaultTo) {
    return getOptions().stream().filter(o -> o.contains(name)).findFirst().orElse(defaultTo);
  }

  @Nullable
  default IOption<?> getOption(String name) {
    return getOption(name, null);
  }

  String getHelpText(IHelpTextFormatter formatter);

  String getSyntaxHelpText();

  /**
   * Array of flags designated to this command
   *
   * @return empty list if command has no flags
   */
  Collection<Enum<? extends ICmdFlag>> getFlags();

  /**
   * Adds a flag to the command
   *
   * @param flag flag
   * @return true if added, otherwise false
   */
  boolean addFlag(Enum<? extends ICmdFlag> flag);

  /**
   * Removes a flag to the command
   *
   * @param flag flag
   * @return true if removed, otherwise false
   */
  boolean removeFlag(Enum<? extends ICmdFlag> flag);

  /**
   * Checks if the command contains the flag
   *
   * @param flag flag
   * @return true if contains, otherwise false
   */
  boolean containsFlag(Enum<? extends ICmdFlag> flag);

  /** Remove all the flags this command has */
  void clearFlags();

  @Nonnull
  Collection<ICmdCallback> getCallbacks();

  @Nonnull
  <T extends ICmdCallback> Collection<T> getCallbacksOfType(Class<T> clazz);

  boolean addCallback(ICmdCallback callback);

  boolean removeCallback(ICmdCallback callback);

  /**
   * Process the command
   *
   * @param args command arguments
   * @return true if the command was processed successfully
   * @throws CmdMissingArgumentException if there are many missing arguments
   */
  boolean process(String[] args)
      throws CmdUnknownException, CmdMissingArgumentException, CmdAmbiguousException;

  @Override
  default String getUniqueHeader() {
    return getAbsoluteName(); // no not overload this, will experience weird results when
    // serializing
  }
}
