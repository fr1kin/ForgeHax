package com.matt.forgehax.util.command.v2.argument;

import com.google.common.collect.ImmutableList;
import com.matt.forgehax.util.Immutables;
import com.matt.forgehax.util.command.v2.ICommandV2;
import com.matt.forgehax.util.typeconverter.TypeConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created on 12/25/2017 by fr1kin
 */
public abstract class ArgumentV2<E> implements InputInterpreter {
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

    public final boolean isOptional() {
        return !isRequired();
    }

    /**
     * Default value if the argument is missing
     * @return null if there is no default value
     */
    @Nullable
    public abstract E getDefaultValue();

    @Nullable
    public E getValue() {
        throw new UnsupportedOperationException("no value set");
    }
    public List<Optional<E>> getValues() {
        return Collections.singletonList(Optional.ofNullable(getValue()));
    }

    public final String getValueAsString() {
        return getConverter().toString(getValue());
    }
    public final List<String> getValuesAsStrings() {
        return getValues().stream()
                .map(v -> getConverter().toString(v.orElse(null)))
                .collect(Collectors.toList());
    }

    public ArgumentV2<E> withValue(E value) {
        return new Value<>(this, defaultIfNull(value));
    }
    public final ArgumentV2<E> withValue(String value) {
        return withValue(getConverter().parse(value));
    }
    public final ArgumentV2<E> withDefaultValue() {
        return withValue(getDefaultValue());
    }

    public boolean hasValue() {
        return false;
    }

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

    public ArgumentV2Builder<E> copy() {
        return new ArgumentV2Builder<E>()
                .description(getDescription())
                .converter(getConverter())
                .required(isRequired())
                .defaultTo(getDefaultValue());
    }

    E defaultIfNull(E value) {
        return value == null ? getDefaultValue() : value;
    }

    @Nonnull
    @Override
    public List<String> getInterpretations(ICommandV2 command, String input) {
        return Collections.emptyList();
    }

    @Override
    public boolean isInterpretable() {
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDescription(), getConverter().type(), isRequired(), getDefaultValue());
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof ArgumentV2 && hashCode() == obj.hashCode());
    }

    static class Value<E> extends ArgumentV2<E> {
        private final ArgumentV2<E> argument;
        private final E value;

        private Value(ArgumentV2<E> argument, E value) {
            this.argument = argument;
            this.value = value;
        }

        @Override
        public String getDescription() {
            return argument.getDescription();
        }

        @Nonnull
        @Override
        public TypeConverter<E> getConverter() {
            return argument.getConverter();
        }

        @Override
        public boolean isRequired() {
            return argument.isRequired();
        }

        @Nullable
        @Override
        public E getDefaultValue() {
            return argument.getDefaultValue();
        }

        @Nullable
        @Override
        public E getValue() {
            return value;
        }

        @Override
        public ArgumentV2<E> withValue(E value) {
            return new ListValues<>(argument, ImmutableList.<Optional<E>>builder()
                    .add(Optional.ofNullable(getValue()))
                    .add(Optional.ofNullable(defaultIfNull(value)))
                    .build());
        }

        @Override
        public boolean hasValue() {
            return true;
        }

        @Nonnull
        @Override
        public List<String> getInterpretations(ICommandV2 command, String input) {
            return argument.getInterpretations(command, input);
        }

        @Override
        public boolean isInterpretable() {
            return argument.isInterpretable();
        }
    }

    static class ListValues<E> extends ArgumentV2<E> {
        private final ArgumentV2<E> argument;
        private final List<Optional<E>> values;

        private ListValues(ArgumentV2<E> argument, List<Optional<E>> values) {
            this.argument = argument;
            this.values = Immutables.copyToList(values);
        }

        private Optional<E> get(int index) {
            return (index > -1 && index < values.size()) ? values.get(index) : Optional.empty();
        }

        @Override
        public String getDescription() {
            return argument.getDescription();
        }

        @Nonnull
        @Override
        public TypeConverter<E> getConverter() {
            return argument.getConverter();
        }

        @Override
        public boolean isRequired() {
            return argument.isRequired();
        }

        @Nullable
        @Override
        public E getDefaultValue() {
            return argument.getDefaultValue();
        }

        @Nullable
        @Override
        public E getValue() throws UnsupportedOperationException {
            return get(0).orElse(getDefaultValue());
        }

        @Override
        public List<Optional<E>> getValues() {
            return values;
        }

        @Override
        public ArgumentV2<E> withValue(E value) {
            return new ListValues<>(argument, ImmutableList.<Optional<E>>builder()
                    .addAll(values)
                    .add(Optional.ofNullable(defaultIfNull(value)))
                    .build());
        }

        @Override
        public boolean hasValue() {
            return true;
        }

        @Nonnull
        @Override
        public List<String> getInterpretations(ICommandV2 command, String input) {
            return argument.getInterpretations(command, input);
        }

        @Override
        public boolean isInterpretable() {
            return argument.isInterpretable();
        }
    }
}
