package com.matt.forgehax.util.command.v2.argument;

import com.matt.forgehax.util.command.v2.ICommandV2;
import com.matt.forgehax.util.command.v2.exception.CommandExceptions;
import com.matt.forgehax.util.command.v2.exception.CommandRuntimeExceptionV2;
import com.matt.forgehax.util.typeconverter.TypeConverter;
import joptsimple.internal.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Created on 2/3/2018 by fr1kin
 */
public class ArgumentV2Generic<E> extends ArgumentV2<E> {
    private final String description;
    private final TypeConverter<E> converter;
    private final boolean required;
    private final E defaultValue;

    protected ArgumentV2Generic(
            @Nullable String description,
            TypeConverter<E> converter,
            boolean required,
            @Nullable E defaultValue) throws CommandRuntimeExceptionV2.CreationFailure {
        CommandExceptions.checkIfNull(converter, "converter is null");
        this.description = Strings.isNullOrEmpty(description) ? "No description given" : description;
        this.converter = converter;
        this.required = required;
        this.defaultValue = defaultValue;
    }

    /**
     * Description of the command
     * @return description
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Way to convert object to a string and from a string
     * @return converter
     */
    @Nonnull
    @Override
    public TypeConverter<E> getConverter() {
        return converter;
    }

    /**
     * If the command is required
     * @return if the command is required
     */
    @Override
    public boolean isRequired() {
        return required;
    }

    /**
     * Default value if the argument is missing
     * @return null if there is no default value
     */
    @Nullable
    @Override
    public E getDefaultValue() {
        return defaultValue;
    }

    //
    //
    //

    public static class Extension<E> extends ArgumentV2Generic<E> {
        private final InputInterpreter.Function<ArgumentV2<E>> predictor;

        protected Extension(@Nullable String description,
                            TypeConverter<E> converter,
                            boolean required,
                            @Nullable E defaultValue,
                            @Nonnull InputInterpreter.Function<ArgumentV2<E>> predictor) throws CommandRuntimeExceptionV2.CreationFailure {
            super(description, converter, required, defaultValue);
            CommandExceptions.checkIfNull(predictor, "interpreter function is null");
            this.predictor = predictor;
        }

        @Override
        public ArgumentV2Builder<E> copy() {
            return super.copy().interpreter(predictor);
        }

        @Nonnull
        @Override
        public List<String> getInterpretations(ICommandV2 command, String input) {
            return predictor.apply(this, command, input);
        }

        @Override
        public boolean isInterpretable() {
            return true;
        }
    }

    public static class Factory {
        public static <T> ArgumentV2<T> make(@Nullable String description,
                                             TypeConverter<T> converter,
                                             boolean required,
                                             @Nullable T defaultValue,
                                             @Nullable InputInterpreter.Function<ArgumentV2<T>> predictor) {
            return predictor == null ? new ArgumentV2Generic<>(description, converter, required, defaultValue)
                    : new ArgumentV2Generic.Extension<>(description, converter, required, defaultValue, predictor);
        }

        public static <T> ArgumentV2<T> make(@Nullable String description,
                                             TypeConverter<T> converter,
                                             boolean required,
                                             @Nullable T defaultValue) {
            return make(description, converter, required, defaultValue, null);
        }
    }
}
