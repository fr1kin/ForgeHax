package com.matt.forgehax.util.command.v2;

import com.google.common.collect.ImmutableList;
import com.matt.forgehax.util.command.v2.argument.ArgumentV2;
import com.matt.forgehax.util.command.v2.argument.OptionV2;
import com.matt.forgehax.util.command.v2.callback.ICommandCallbackV2;
import com.matt.forgehax.util.command.v2.exception.CommandRuntimeExceptionV2;
import com.matt.forgehax.util.command.v2.flag.ICommandFlagV2;
import com.matt.forgehax.util.serialization.ISerializableJson;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created on 12/25/2017 by fr1kin
 */

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
public interface ICommandV2 extends ISerializableJson {
    String[]            EMPTY_ALIASES           = new String[0];
    ArgumentV2<?>[]     EMPTY_ARGUMENTS         = new ArgumentV2[0];
    OptionV2[]          EMPTY_OPTIONS           = new OptionV2[0];

    /**
     * The formal name for this command
     * @return formal name
     */
    String getName();

    String getAbsoluteName();

    /**
     * Alternative names for this command.
     * @return list of alternative names or an empty array list if none exist.
     */
    @Nonnull
    Collection<String> getAliases();

    /**
     * Returns names and aliases
     * @return list of names and aliases
     */
    default Collection<String> getAllNames() {
        return getAliases().isEmpty() ? Collections.singleton(getName()) :
                ImmutableList.<String>builder()
                        .add(getName())
                        .addAll(getAliases())
                        .build();
    }

    /**
     * If this command can be identified by the given name.
     * Command names are case insensitive.
     * @param name
     * @return
     */
    default boolean isIdentifiableAs(String name) {
        return CommandHelperV2.isNameValid(name) && (
                getName().equalsIgnoreCase(name)
                        || getAbsoluteName().equalsIgnoreCase(name)
                        || getAliases().stream().anyMatch(alias -> alias.equalsIgnoreCase(name)));
    }

    /**
     * Check if these two commands have conflicting naming schemes
     * @param other
     * @return
     */
    default boolean isConflictingWith(ICommandV2 other) {
        return getName().equalsIgnoreCase(other.getName()) || (
                !other.getAliases().isEmpty() // we don't need to evaluate this.getAliases() as anyMatch will do that for us
                        && getAliases().stream().anyMatch(alias -> other.getAliases().stream().anyMatch(alias::equalsIgnoreCase)));
    }

    /**
     * Commands description
     * @return description
     */
    String getDescription();

    /**
     * The parent of this command
     * @return null if no parent
     */
    @Nullable
    IParentCommandV2 getParent();

    List<ArgumentV2<?>> getArguments();
    List<OptionV2> getOptions();

    String getHelpText(IHelpTextFormatter formatter);
    String getSyntaxHelpText();

    /**
     * Array of flags designated to this command
     * @return empty list if command has no flags
     */
    Collection<ICommandFlagV2> getFlags();

    /**
     * Adds a flag to the command
     * @param flag flag
     * @return true if added, otherwise false
     */
    boolean addFlag(ICommandFlagV2 flag);

    /**
     * Removes a flag to the command
     * @param flag flag
     * @return true if removed, otherwise false
     */
    boolean removeFlag(ICommandFlagV2 flag);

    /**
     * Checks if the command contains the flag
     * @param flag flag
     * @return true if contains, otherwise false
     */
    boolean containsFlag(ICommandFlagV2 flag);

    @Nonnull
    Collection<ICommandCallbackV2> getCallbacks();
    @Nonnull
    <T extends ICommandCallbackV2> Collection<T> getCallbacksOfType(Class<T> clazz);

    boolean addCallback(ICommandCallbackV2 callback);
    boolean removeCallback(ICommandCallbackV2 callback);

    CommandBuilderV2 copy(IParentCommandV2 parent);

    /**
     * Process the command
     * @param args command arguments
     * @return true if the command was processed successfully
     * @throws CommandRuntimeExceptionV2.ProcessingFailure for any processing exceptions
     */
    boolean process(String[] args) throws CommandRuntimeExceptionV2.ProcessingFailure;
}
