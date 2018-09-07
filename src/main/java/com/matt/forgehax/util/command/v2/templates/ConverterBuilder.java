package com.matt.forgehax.util.command.v2.templates;

import com.matt.forgehax.util.typeconverter.TypeConverter;
import com.matt.forgehax.util.typeconverter.TypeConverterRegistry;

import java.util.Objects;

/**
 * Created on 4/14/2018 by fr1kin
 */
public interface ConverterBuilder<T, R> {
    /**
     * Converter for changing objects to a string and an string to an object
     * @param typeConverter converter instance
     * @return this
     */
    R converter(TypeConverter<T> typeConverter);

    /**
     * Alternative to using ::converter, will try and lookup the class in the type converter registry.
     * If it fails to find anything, this.converter will be null and CmdRuntimeException.CreationFailure
     * will be thrown when build() is called.
     * @param clazz class of converter
     * @return this
     */
    default R converterOfType(Class<T> clazz) {
        TypeConverter<T> tc = TypeConverterRegistry.get(clazz);
        Objects.requireNonNull(tc, "Failed to find type converter in registry for '" + clazz.getName() + "'");
        return converter(tc);
    }
}
