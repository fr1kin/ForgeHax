package com.matt.forgehax.util.command.v2.argument;

import com.matt.forgehax.util.command.v2.ICommandV2;
import com.matt.forgehax.util.command.v2.exception.CommandExceptions;
import com.matt.forgehax.util.command.v2.exception.CommandRuntimeExceptionV2;
import joptsimple.internal.Strings;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Created on 2/3/2018 by fr1kin
 */
public abstract class OptionV2 implements IPredictableArgument {
    protected static final String NO_DESCRIPTION = "No description given";

    public enum Type {
        /**
         * No argument expected
         */
        FLAG,

        /**
         * Argument can exist, but doesn't need to
         */
        OPTIONAL_ARGUMENT,

        /**
         * Argument is required
         */
        REQUIRED_ARGUMENT,
        ;
    }

    public abstract Type getType();

    /**
     * List of possible names for this option
     * @return non null array
     */
    @Nonnull
    public abstract List<String> getNames();

    /**
     * Description for this option
     * @return NO_DESCRIPTION is description is null
     */
    @Nonnull
    public abstract String getDescription();

    /**
     * Is this option required?
     * @return true if required
     */
    public abstract boolean isRequired();

    /**
     * Gets the optional argument
     * @return argument
     */
    @Nonnull
    public abstract ArgumentV2<?> getArgument();

    /**
     * Copy this options contents into a mutable builder
     * @return new builder instance
     */
    public abstract OptionV2Builder copy();

    /**
     * Checks if any of the provided name any of the matches these options names.
     * This method is case sensitive
     * @param name name to check
     * @return true if a match is found
     */
    public boolean contains(String name) {
        if(Strings.isNullOrEmpty(name)) return false;
        for(String n : getNames()) if(Objects.equals(name, n)) return true;
        return false;
    }

    /**
     * Checks if any of the provided arguments matches the options names.
     * This method is case sensitive
     * @param names list of names to check
     * @return true if a match is found
     */
    public boolean contains(String[] names) {
        for(String n : names) if(contains(n))
            return true;
        return false;
    }

    /**
     * Checks if any of the provided arguments matches the options names.
     * This method is case sensitive
     * @param names list of names to check
     * @return true if a match is found
     */
    public boolean contains(Collection<String> names) {
        for(String n : names) if(contains(n)) return true;
        return false;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(getNames(), getDescription());
    }

    @Override
    public final boolean equals(Object obj) {
        return obj instanceof OptionV2 && hashCode() == obj.hashCode();
    }

    @Nonnull
    @Override
    public List<String> getPredictions(ICommandV2 command, String input) {
        return Collections.emptyList();
    }

    //
    //
    //

    protected static void requireValidNames(Collection<String> names) throws CommandRuntimeExceptionV2 {
        CommandExceptions.checkIfNull(names, "names is null");
        if(names.isEmpty()) throw new CommandRuntimeExceptionV2.CreationFailure("no name(s) provided");
        for(String n : names) if(Strings.isNullOrEmpty(n))
            throw new CommandRuntimeExceptionV2.CreationFailure("provided name is null or empty");
    }
}
