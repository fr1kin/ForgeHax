package com.matt.forgehax.util.command.v2.argument;

import com.matt.forgehax.util.command.v2.exception.CommandRuntimeExceptionV2;
import com.matt.forgehax.util.typeconverter.TypeConverter;
import com.matt.forgehax.util.typeconverter.TypeConverterRegistry;

/**
 * Created on 1/30/2018 by fr1kin
 */
public class ArgumentV2Builder<E> {
    private String description = null;
    private TypeConverter<E> converter = null;
    private boolean required = true;
    private E defaultValue = null;
    private ISuggestionProvider.Function<ArgumentV2<E>> suggestionsFunction = null;

    /**
     * Short description of what this argument exists for
     * @param description description
     * @return this
     */
    public ArgumentV2Builder<E> description(String description) {
        this.description = description;
        return this;
    }

    /**
     * Converter for changing objects to a string and an string to an object
     * @param converter converter instance
     * @return this
     */
    public ArgumentV2Builder<E> converter(TypeConverter<E> converter) {
        this.converter = converter;
        return this;
    }

    /**
     * Alternative to using ::converter, will try and lookup the class in the type converter registry.
     * If it fails to find anything, this.converter will be null and CommandRuntimeExceptionV2.CreationFailure
     * will be thrown when build() is called.
     * @param clazz class of converter
     * @return this
     */
    public ArgumentV2Builder<E> type(Class<E> clazz) {
        return converter(TypeConverterRegistry.get(clazz));
    }

    /**
     * If the argument is required
     * @param required required
     * @return this
     */
    public ArgumentV2Builder<E> required(boolean required) {
        this.required = required;
        return this;
    }

    /**
     * If the argument is missing, it can default to this
     * @param defaultValue default value to default to
     * @return this
     */
    public ArgumentV2Builder<E> defaultTo(E defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    /**
     * Generates list of possible options given a certain input
     * @param suggestionsFunction function
     * @return this
     */
    public ArgumentV2Builder<E> suggestions(ISuggestionProvider.Function<ArgumentV2<E>> suggestionsFunction) {
        this.suggestionsFunction = suggestionsFunction;
        return this;
    }

    /**
     * Build argument
     * @return new argument instance
     * @throws CommandRuntimeExceptionV2.CreationFailure if there is anything wrong with the provided data
     */
    public ArgumentV2<E> build() throws CommandRuntimeExceptionV2.CreationFailure {
        return suggestionsFunction == null ? new ArgumentV2Generic<>(description, converter, required, defaultValue)
                : new ArgumentV2Generic.InputHelper<>(description, converter, required, defaultValue, suggestionsFunction);
    }

    public ArgumentV2 empty() throws CommandRuntimeExceptionV2.CreationFailure {
        return ArgumentV2Empty.getInstance();
    }
}
