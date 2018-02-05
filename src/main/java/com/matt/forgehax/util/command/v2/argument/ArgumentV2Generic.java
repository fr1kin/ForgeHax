package com.matt.forgehax.util.command.v2.argument;

import com.matt.forgehax.util.command.v2.exception.CommandRuntimeExceptionV2;
import com.matt.forgehax.util.typeconverter.TypeConverter;
import joptsimple.internal.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
        Objects.requireNonNull(converter, "converter is null");
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

    @Nonnull
    @Override
    public List<String> getSuggestions(String input) {
        return Collections.emptyList();
    }

    //
    //
    //

    public static class InputHelper<E> extends ArgumentV2Generic<E> {
        private final ISuggestionProvider.Function<ArgumentV2<E>> suggestionsFunction;

        protected InputHelper(@Nullable String description,
                              TypeConverter<E> converter,
                              boolean required,
                              @Nullable E defaultValue,
                              @Nonnull ISuggestionProvider.Function<ArgumentV2<E>> suggestionsFunction) throws CommandRuntimeExceptionV2.CreationFailure {
            super(description, converter, required, defaultValue);
            Objects.requireNonNull(suggestionsFunction, "suggestions function is null");
            this.suggestionsFunction = suggestionsFunction;
        }

        @Nonnull
        @Override
        public List<String> getSuggestions(String input) {
            return suggestionsFunction.apply(this, input);
        }
    }
}
