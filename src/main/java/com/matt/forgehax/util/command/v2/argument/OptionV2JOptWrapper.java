package com.matt.forgehax.util.command.v2.argument;

/**
 * Created on 2/3/2018 by fr1kin
 */

import com.matt.forgehax.util.command.v2.exception.CommandRuntimeExceptionV2;
import com.matt.forgehax.util.typeconverter.TypeConverter;
import com.matt.forgehax.util.typeconverter.TypeConverterRegistry;
import joptsimple.OptionDescriptor;
import joptsimple.ValueConverter;
import joptsimple.internal.Reflection;
import org.apache.logging.log4j.core.util.Loader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/**
 * Wraps around JOpt OptionSpec
 */
public class OptionV2JOptWrapper extends OptionV2 {
    private final OptionDescriptor descriptor;

    protected OptionV2JOptWrapper(@Nonnull OptionDescriptor descriptor) throws CommandRuntimeExceptionV2.CreationFailure {
        this.descriptor = descriptor;
    }

    public OptionDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public Type getType() {
        return Type.FLAG;
    }

    @Nonnull
    @Override
    public List<String> getNames() {
        return descriptor.options();
    }

    @Nonnull
    @Override
    public String getDescription() {
        return descriptor.description();
    }

    @Override
    public boolean isRequired() {
        return descriptor.isRequired();
    }

    @Nonnull
    @Override
    public ArgumentV2<?> getArgument() {
        return ArgumentV2Empty.getInstance();
    }

    @Override
    public OptionV2Builder copy() {
        return new OptionV2Builder()
                .fromDescriptor(getDescriptor())
                .required(isRequired())
                .argument(getArgument());
    }

    //
    //
    //

    public static class AcceptsArgument extends OptionV2JOptWrapper {
        private final ArgumentV2<?> argument;

        protected AcceptsArgument(@Nonnull OptionDescriptor descriptor, @Nullable ArgumentV2<?> argument) throws CommandRuntimeExceptionV2.CreationFailure {
            super(descriptor);

            if(!descriptor.acceptsArguments()) throw new CommandRuntimeExceptionV2.CreationFailure("descriptor does not accept arguments");

            if(!ArgumentHelper.isNullOrEmpty(argument))
                this.argument = argument;
            else {
                // try and look up the converter
                final String className = descriptor.argumentTypeIndicator();
                TypeConverter<?> cv = TypeConverterRegistry.getByName(className);
                if (cv == null) {
                    // didn't work, try and use JOpts converter and make a type converter wrapper for it
                    try {
                        Class<?> clazz = Class.forName(className, true, Loader.getClassLoader());
                        ValueConverter<?> vc = Reflection.findConverter(clazz);
                        cv = new TypeConverter<Object>() {
                            @Override
                            public String label() {
                                return vc.valueType().getName();
                            }

                            @Override
                            public Class<Object> type() {
                                return (Class) vc.valueType();
                            }

                            @Override
                            public Object parse(String value) {
                                return vc.convert(value);
                            }

                            @Override
                            public String toString(Object value) {
                                return String.valueOf(value);
                            }
                        };
                    } catch (Throwable t) {
                        throw new CommandRuntimeExceptionV2.CreationFailure("could not create a type converter");
                    }
                }
                final TypeConverter converter = cv;

                this.argument = new ArgumentV2<Object>() {
                    @Override
                    public String getDescription() {
                        return descriptor.argumentDescription();
                    }

                    @Nonnull
                    @Override
                    public TypeConverter<Object> getConverter() {
                        return converter;
                    }

                    @Override
                    public boolean isRequired() {
                        return descriptor.requiresArgument();
                    }

                    @Nullable
                    @Override
                    public Object getDefaultValue() {
                        List values = descriptor.defaultValues();
                        return values.isEmpty() ? null : values.get(0); // only supports 1 default value atm
                    }
                };
            }
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

        public static class Suggestions extends AcceptsArgument {
            private final ISuggestionProvider.Function<OptionV2> suggestionsFunction;

            protected Suggestions(@Nonnull OptionDescriptor descriptor, @Nullable ArgumentV2<?> argument, @Nonnull ISuggestionProvider.Function<OptionV2> suggestionsFunction) throws NullPointerException, CommandRuntimeExceptionV2.CreationFailure {
                super(descriptor, argument);
                Objects.requireNonNull(suggestionsFunction, "suggestions function is null");
                this.suggestionsFunction = suggestionsFunction;
            }

            @Nonnull
            @Override
            public List<String> getSuggestions(String input) {
                return suggestionsFunction.apply(this, input);
            }

            @Override
            public OptionV2Builder copy() {
                return super.copy().suggestions(suggestionsFunction);
            }
        }
    }

    public static class Suggestions extends OptionV2JOptWrapper {
        private final ISuggestionProvider.Function<OptionV2> suggestionsFunction;

        protected Suggestions(@Nonnull OptionDescriptor descriptor, @Nonnull ISuggestionProvider.Function<OptionV2> suggestionsFunction) throws NullPointerException, CommandRuntimeExceptionV2.CreationFailure {
            super(descriptor);
            Objects.requireNonNull(suggestionsFunction, "suggestions function is null");
            this.suggestionsFunction = suggestionsFunction;
        }

        @Nonnull
        @Override
        public List<String> getSuggestions(String input) {
            return suggestionsFunction.apply(this, input);
        }

        @Override
        public OptionV2Builder copy() {
            return super.copy().suggestions(suggestionsFunction);
        }
    }
}
