package com.matt.forgehax.util.command.v2.argument;

import com.matt.forgehax.util.CaseSensitive;
import com.matt.forgehax.util.Immutables;
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
public abstract class OptionV2<E> implements InputInterpreter {
    protected static final String NO_DESCRIPTION = "<none>";

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

    public List<String> getShortNames() {
        return getNames().stream()
                .filter(n -> n.length() == 1)
                .collect(Immutables.toImmutableList());
    }

    public List<String> getFullNames() {
        return getNames().stream()
                .filter(n -> n.length() > 1)
                .collect(Immutables.toImmutableList());
    }

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
    public abstract ArgumentV2<E> getArgument();

    public final boolean hasArgument() {
        return !getType().equals(Type.FLAG);
    }

    public OptionV2<E> appendValue(E value) {
        return new ArgValue<>(this, getArgument().withValue(value));
    }
    public final OptionV2<E> appendValue(String value) {
        return appendValue(getArgument().getConverter().parse(value));
    }
    public final OptionV2<E> appendDefaultValue() {
        return appendValue(getArgument().getDefaultValue());
    }

    /**
     * Copy this options contents into a mutable builder
     * @return new builder instance
     */
    public abstract OptionV2Builder<E> copy();

    /**
     * Checks if any of the provided name any of the matches these options names.
     * This method is case sensitive
     * @param name name to check
     * @return true if a match is found
     */
    public boolean contains(@CaseSensitive String name) {
        if(Strings.isNullOrEmpty(name)) return false;
        for(String n : getNames()) if(name.equals(n)) return true;
        return false;
    }

    /**
     * Checks if any of the provided arguments matches the options names.
     * This method is case sensitive
     * @param names list of names to check
     * @return true if a match is found
     */
    public boolean contains(@CaseSensitive String[] names) {
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
    public boolean contains(@CaseSensitive Collection<String> names) {
        for(String n : names) if(contains(n)) return true;
        return false;
    }

    public boolean matches(OptionV2<?> option) {
        return contains(option.getNames());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(getNames());
    }

    @Override
    public final boolean equals(Object obj) {
        return this == obj || (obj instanceof OptionV2 && matches((OptionV2)obj));
    }

    @Nonnull
    @Override
    public List<String> getInterpretations(ICommandV2 command, String input) {
        return Collections.emptyList();
    }

    @Override
    public boolean isInterpretable() {
        return false;
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

    static class ArgValue<E> extends OptionV2<E> {
        private final OptionV2<E> option;
        private final ArgumentV2<E> argument;

        public ArgValue(OptionV2<E> option, ArgumentV2<E> argument) {
            this.option = option;
            this.argument = argument;
        }

        @Override
        public Type getType() {
            return option.getType();
        }

        @Nonnull
        @Override
        public List<String> getNames() {
            return option.getNames();
        }

        @Nonnull
        @Override
        public String getDescription() {
            return option.getDescription();
        }

        @Override
        public boolean isRequired() {
            return option.isRequired();
        }

        @Nonnull
        @Override
        public ArgumentV2<E> getArgument() {
            return argument;
        }

        @Override
        public OptionV2<E> appendValue(E value) {
            return new ArgValue<>(option, argument.withValue(value));
        }

        @Override
        public OptionV2Builder<E> copy() {
            return option.copy().argument(argument);
        }

        @Nonnull
        @Override
        public List<String> getInterpretations(ICommandV2 command, String input) {
            return option.getInterpretations(command, input);
        }

        @Override
        public boolean isInterpretable() {
            return option.isInterpretable();
        }
    }
}
