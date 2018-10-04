package com.matt.forgehax.util.command.v2.converter;

import com.matt.forgehax.util.serialization.ISerializableImmutable;
import java.util.Comparator;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public interface IConverter<E>
    extends ValuesConverter<E>,
        ValueConverter<E>,
        StringConverter<E>,
        Comparator<E>,
        ISerializableImmutable<E> {
  /**
   * Class type this object is converting
   *
   * @return class
   */
  Class<E> getType();

  /**
   * Gets a primitive class that also represents this
   *
   * @return null if this is not a primitive
   */
  @Nullable
  default Class<?> getPrimitiveType() {
    return null;
  }

  /**
   * A friendly label that maybe used to describe the type
   *
   * @return label string
   */
  String getLabel();

  /**
   * Create a builder that copies this instance.
   *
   * @return new builder
   */
  default Builder<E> builder() {
    return ConverterFactory.<E>newBuilder()
        .type(getType())
        .primitiveType(getPrimitiveType())
        .label(getLabel())
        .comparator(this)
        .valuesOf(this)
        .valueOf(this)
        .toString(this)
        .serializer(this);
  }

  final class Builder<E> {
    private Class<E> _type;
    private Class<?> _primitiveType;
    private String _label;
    private Comparator<E> _comparator;
    private ValuesConverter<E> _valuesConverter;
    private ValueConverter<E> _valueConverter;
    private StringConverter<E> _stringConverter;
    private ISerializableImmutable<E> _serializer;

    Builder() {}

    /**
     * Class type this converter represents
     *
     * @param type class
     * @return this
     */
    public Builder<E> type(Class<E> type) {
      this._type = type;
      return this;
    }

    /**
     * Primitive class type of this converter. Only needed for Java primitives.
     *
     * @param primitiveType primitive class
     * @return this
     */
    public Builder<E> primitiveType(Class<?> primitiveType) {
      this._primitiveType = primitiveType;
      return this;
    }

    /**
     * A label used to identify this object
     *
     * @param label short name
     * @return this
     */
    public Builder<E> label(String label) {
      this._label = label;
      return this;
    }

    /**
     * Comparator used to compare two values against each other
     *
     * @param comparator comparator
     * @return this
     */
    public Builder<E> comparator(Comparator<E> comparator) {
      this._comparator = comparator;
      return this;
    }

    /**
     * A function that collects all the possible values from a given string. If not provided, one
     * will be automatically generated provided valueOf() is nonnull.
     *
     * @param function conversion function
     * @return this
     */
    public Builder<E> valuesOf(ValuesConverter<E> function) {
      this._valuesConverter = function;
      return this;
    }

    /**
     * A function that gets the most probable value of this object from a given string. If not
     * provided, one will be automatically generated provided valuesOf() is nonnull.
     *
     * @param function conversion function
     * @return this
     */
    public Builder<E> valueOf(ValueConverter<E> function) {
      this._valueConverter = function;
      return this;
    }

    /**
     * A function used to translate an instance to a string.
     *
     * @param function to string function
     * @return this
     */
    public Builder<E> toString(StringConverter<E> function) {
      this._stringConverter = function;
      return this;
    }

    public Builder<E> serializer(ISerializableImmutable<E> serializer) {
      this._serializer = serializer;
      return this;
    }

    /**
     * Build the converter
     *
     * @return new converter instance
     */
    public IConverter<E> build() {
      if (_valueConverter == null && _valuesConverter == null)
        throw new NullPointerException(
            "Cannot build without a value and/or values converter provided");

      if (_valuesConverter == null)
        _valuesConverter = ((input, stream) -> Stream.of(_valueConverter.valueOf(input)));

      if (_valueConverter == null)
        _valueConverter =
            ((input, stream) -> _valuesConverter.valuesOf(input).findFirst().orElse(null));

      return new BaseConverter<>(
          _type,
          _primitiveType,
          _label,
          _comparator,
          _valuesConverter,
          _valueConverter,
          _stringConverter,
          _serializer);
    }
  }
}
