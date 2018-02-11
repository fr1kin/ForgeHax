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
public class OptionV2Generic extends OptionV2 {
    private final List<String> names;
    private final String description;
    private final boolean required;

    protected OptionV2Generic(Collection<String> names, @Nullable String description, boolean required) throws CommandRuntimeExceptionV2 {
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

    @Nonnull
    @Override
    public ArgumentV2<?> getArgument() {
        return ArgumentV2Empty.getInstance();
    }

    @Override
    public OptionV2Builder copy() {
        return new OptionV2Builder()
                .names(getNames())
                .description(getDescription())
                .required(isRequired())
                .argument(getArgument());
    }

    //
    //
    //

    public static class AcceptsArgument extends OptionV2Generic {
        private final ArgumentV2<?> argument;

        protected AcceptsArgument(Collection<String> names, @Nullable String description, boolean required, @Nonnull ArgumentV2<?> argument) throws CommandRuntimeExceptionV2 {
            super(names, description, required);
            CommandExceptions.checkIfNull(argument, "argument provided is null");
            this.argument = argument;
        }

        @Override
        public Type getType() {
            return argument.isRequired() ? Type.REQUIRED_ARGUMENT : Type.OPTIONAL_ARGUMENT;
        }

        @Nonnull
        @Override
        public ArgumentV2<?> getArgument() {
            return argument;
        }

        //
        //
        //

        public static class Extension extends AcceptsArgument {
            private final IPredictableArgument.Function<OptionV2> predictor;

            protected Extension(Collection<String> names, @Nullable String description, boolean required, @Nonnull ArgumentV2<?> argument, @Nonnull IPredictableArgument.Function<OptionV2> predictor) throws CommandRuntimeExceptionV2 {
                super(names, description, required, argument);
                CommandExceptions.checkIfNull(predictor, "predictor function is null");
                this.predictor = predictor;
            }

            @Nonnull
            @Override
            public List<String> getPredictions(ICommandV2 command, String input) {
                return predictor.apply(this, command, input);
            }

            @Override
            public OptionV2Builder copy() {
                return super.copy().predictor(predictor);
            }
        }
    }
}
