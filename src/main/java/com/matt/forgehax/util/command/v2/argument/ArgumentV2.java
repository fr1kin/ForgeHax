package com.matt.forgehax.util.command.v2.argument;

import com.matt.forgehax.util.typeconverter.TypeConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Created on 12/25/2017 by fr1kin
 */
public abstract class ArgumentV2<E> implements ISuggestionProvider {
    /**
     * Description of the command
     * @return description
     */
    public abstract String getDescription();

    /**
     * Way to convert object to a string and from a string
     * @return converter
     */
    @Nonnull
    public abstract TypeConverter<E> getConverter();

    /**
     * If the argument is required
     * @return
     */
    public abstract boolean isRequired();

    /**
     * Default value if the argument is missing
     * @return null if there is no default value
     */
    @Nullable
    public abstract E getDefaultValue();

    @SuppressWarnings("unchecked")
    public <T> ArgumentV2<T> cast() throws ClassCastException {
        return getClass().cast(this);
    }
    @SuppressWarnings("unchecked")
    public <T> ArgumentV2<T> cast(T o) throws ClassCastException {
        return getClass().cast(this);
    }
    @SuppressWarnings("unchecked")
    public <T> ArgumentV2<T> cast(Class<T> o) throws ClassCastException {
        return getClass().cast(this);
    }

    @Nonnull
    @Override
    public List<String> getSuggestions(String input) {
        return Collections.emptyList();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDescription(), isRequired());
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof ArgumentV2 && hashCode() == obj.hashCode());
    }
}
