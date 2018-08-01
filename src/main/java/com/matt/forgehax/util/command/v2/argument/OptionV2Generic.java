package com.matt.forgehax.util.command.v2.argument;

import com.google.common.collect.ImmutableList;
import com.matt.forgehax.util.command.v2.ICommandV2;
import com.matt.forgehax.util.command.v2.exception.CommandExceptions;
import com.matt.forgehax.util.command.v2.exception.CommandRuntimeExceptionV2;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

/**
 * Created on 12/25/2017 by fr1kin
 */
public class OptionV2Generic<E> extends OptionV2<E> {
    private final List<String> names;
    private final String description;
    private final boolean required;

    private OptionV2Generic(Collection<String> names,
                              @Nullable String description,
                              boolean required) throws CommandRuntimeExceptionV2 {
        requireValidNames(names);
        this.names = ImmutableList.copyOf(names);
        this.description = description == null ? NO_DESCRIPTION : description;
        this.required = required;
    }

    @Override
    public Type getType() {
        return Type.FLAG;
    }

    @Nonnull
    @Override
    public List<String> getNames() {
        return names;
    }

    @Nonnull
    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isRequired() {
        return required;
    }

    @Override
    public ArgumentV2<E> getArgument() {
        return ArgumentV2Empty.getInstance();
    }

    @Override
    public OptionV2Builder<E> copy() {
        return new OptionV2Builder<E>()
                .names(getNames())
                .description(getDescription())
                .required(isRequired())
                .argument(getArgument());
    }

    //
    //
    //

    public static class AcceptsArgument<E> extends OptionV2Generic<E> {
        private final ArgumentV2<E> argument;
        private final InputInterpreter.Function<OptionV2<E>> predictor;

        private AcceptsArgument(Collection<String> names,
                                  @Nullable String description,
                                  boolean required,
                                  @Nonnull ArgumentV2<E> argument,
                                  @Nullable InputInterpreter.Function<OptionV2<E>> predictor) throws CommandRuntimeExceptionV2 {
            super(names, description, required);
            CommandExceptions.checkIfNull(argument, "argument provided is null");
            this.argument = argument;
            this.predictor = predictor;
        }

        @Override
        public Type getType() {
            return argument.isRequired() ? Type.REQUIRED_ARGUMENT : Type.OPTIONAL_ARGUMENT;
        }

        @Override
        public ArgumentV2<E> getArgument() {
            return argument;
        }

        @Nonnull
        @Override
        public List<String> getInterpretations(ICommandV2 command, String input) {
            return predictor == null ? super.getInterpretations(command, input) : predictor.apply(this, command, input);
        }

        @Override
        public boolean isInterpretable() {
            return predictor != null;
        }

        @Override
        public OptionV2Builder<E> copy() {
            return super.copy().interpreter(predictor).argument(argument);
        }
    }

    public static class Factory {
        public static <T> OptionV2<T> make(Collection<String> names,
                                    @Nullable String description,
                                    boolean required,
                                    @Nullable ArgumentV2<T> argument,
                                    @Nullable InputInterpreter.Function<OptionV2<T>> predictor) {
            return ArgumentHelper.isNullOrEmpty(argument) ? new OptionV2Generic<>(names, description, required)
                    : new OptionV2Generic.AcceptsArgument<>(names, description, required, argument, predictor);
        }

        public static <T> OptionV2<T> make(Collection<String> names,
                                    @Nullable String description,
                                    boolean required) {
            return make(names, description, required, null, null);
        }
    }
}
