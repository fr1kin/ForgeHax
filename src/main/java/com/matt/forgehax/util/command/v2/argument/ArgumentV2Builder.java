package com.matt.forgehax.util.command.v2.argument;

import com.matt.forgehax.util.command.v2.exception.CommandRuntimeExceptionV2;
import com.matt.forgehax.util.command.v2.templates.ConverterBuilder;
import com.matt.forgehax.util.typeconverter.TypeConverter;

/**
 * Created on 1/30/2018 by fr1kin
 */
public class ArgumentV2Builder<E> implements ConverterBuilder<E, ArgumentV2Builder<E>> {
    private String description = null;
    private TypeConverter<E> converter = null;
    private boolean required = true;
    private E defaultValue = null;
    private InputInterpreter.Function<ArgumentV2<E>> predictor = null;

    @Override
    public ArgumentV2Builder<E> converter(TypeConverter<E> typeConverter) {
        this.converter = typeConverter;
        return this;
    }

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
     * If the argument is required
     * @param required required
     * @return this
     */
    public ArgumentV2Builder<E> required(boolean required) {
        this.required = required;
        return this;
    }
    public ArgumentV2Builder<E> required() {
        return required(true);
    }
    public ArgumentV2Builder<E> notRequired() {
        return required(false);
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
     * @param predictor function
     * @return this
     */
    public ArgumentV2Builder<E> interpreter(InputInterpreter.Function<ArgumentV2<E>> predictor) {
        this.predictor = predictor;
        return this;
    }

    /**
     * Build argument
     * @return new argument instance
     * @throws CommandRuntimeExceptionV2.CreationFailure if there is anything wrong with the provided data
     */
    public ArgumentV2<E> build() throws CommandRuntimeExceptionV2.CreationFailure {
        return ArgumentV2Generic.Factory.make(description, converter, required, defaultValue, predictor);
    }

    public ArgumentV2 empty() throws CommandRuntimeExceptionV2.CreationFailure {
        return ArgumentV2Empty.getInstance();
    }
}
