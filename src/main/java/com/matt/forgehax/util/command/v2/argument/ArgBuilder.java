package com.matt.forgehax.util.command.v2.argument;

import com.google.common.base.MoreObjects;
import com.matt.forgehax.util.command.v2.converter.Converters;
import com.matt.forgehax.util.command.v2.converter.IConverter;

import java.util.Collections;

public class ArgBuilder<E> {
    public static <T> ArgMap<T> newArgMap(IArg<T> parent) {
        return new ArgMap<>(parent);
    }

    //
    //
    //

    private String _description;
    private String _shortDescription;
    private boolean _required;
    private E _defaultValue;
    private IConverter<E> _converter;

    public ArgBuilder() {}

    ArgBuilder<E> set_converter(IConverter<E> converter) {
        this._converter = MoreObjects.firstNonNull(this._converter, converter);
        return this;
    }

    /**
     * Description for this argument. Preferably with details.
     * Not required. If not provided, it will use the short description.
     * @param description description of this argument
     * @return this
     */
    public ArgBuilder<E> description(String description) {
        this._description = description;
        this._shortDescription = MoreObjects.firstNonNull(_shortDescription, description);
        return this;
    }

    /**
     * A shorter description that can be used in the syntax helper.
     * Not required. If not provided, it will use the description.
     * @param shortDescription The shortest possible description for this argument.
     * @return this
     */
    public ArgBuilder<E> shortDescription(String shortDescription) {
        this._shortDescription = shortDescription;
        this._description = MoreObjects.firstNonNull(_description, shortDescription);
        return this;
    }

    /**
     * Flag this argument as required
     * @return this
     */
    public ArgBuilder<E> required() {
        this._required = true;
        return this;
    }

    /**
     * Flag this argument as optional
     * @return this
     */
    public ArgBuilder<E> optional() {
        this._required = false;
        return this;
    }

    /**
     * The default value for this argument.
     * Not required and can be null.
     * Will automatically set the converter if that has not been done already.
     * @param defaultValue default value
     * @return this
     */
    public ArgBuilder<E> defaultValue(E defaultValue) {
        this._defaultValue = defaultValue;
        return set_converter(Converters.get(defaultValue).orElse(null));
    }

    /**
     * The converter used to change this value to and from a string.
     * @param converter converter instance
     * @return this
     */
    public ArgBuilder<E> converter(IConverter<E> converter) {
        this._converter = converter;
        return this;
    }

    /**
     * Build the argument.
     * @return new argument instance
     */
    public IArg<E> build() {
        return new BaseArg<>(_description, _shortDescription, _required, _defaultValue, _converter);
    }
}
