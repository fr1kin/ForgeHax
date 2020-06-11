package com.matt.forgehax.util.command;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.matt.forgehax.mods.services.ForgeHaxService;
import com.matt.forgehax.util.SafeConverter;
import com.matt.forgehax.util.command.callbacks.OnChangeCallback;
import com.matt.forgehax.util.command.exception.CommandBuildException;
import com.matt.forgehax.util.console.ConsoleIO;
import com.matt.forgehax.util.serialization.ISerializableJson;
import com.matt.forgehax.util.typeconverter.TypeConverter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created on 6/2/2017 by fr1kin
 */
public class Setting<E> extends Command implements ISerializableJson {

  public static final String DEFAULTVALUE = "Setting.defaultValue";
  public static final String CONVERTER = "Setting.converter";
  public static final String COMPARATOR = "Setting.comparator";
  public static final String MINVALUE = "Setting.minvalue";
  public static final String MAXVALUE = "Setting.maxvalue";
  public static final String RESETAUTOGEN = "Setting.resetAutoGen";
  public static final String DEFAULTPROCESSOR = "Setting.defaultProcessor";

  private final E defaultValue;
  private final TypeConverter<E> converter;
  private final Comparator<E> comparator;
  private final E minValue;
  private final E maxValue;

  private E value;

  @SuppressWarnings("unchecked")
  protected Setting(Map<String, Object> data) throws CommandBuildException {
    super(data);
    try {
      this.converter = (TypeConverter<E>) data.get(CONVERTER);
      Objects.requireNonNull(this.converter, "Setting requires converter");

      this.defaultValue = (E) data.get(DEFAULTVALUE);
      this.comparator = (Comparator<E>) data.get(COMPARATOR);
      this.minValue = (E) data.get(MINVALUE);
      this.maxValue = (E) data.get(MAXVALUE);

      Boolean defaultProcessor = (Boolean) data.getOrDefault(DEFAULTPROCESSOR, true);
      if (defaultProcessor) {
        processors.add(
            in -> {
              in.requiredArguments(1);
              Object arg = in.getArgument(0);
              if (arg != null) {
                rawSet(String.valueOf(arg));
                serialize();
                in.markSuccess();
              } else {
                in.markFailed();
              }
            });
      }

      Boolean resetAutoGen = (Boolean) data.getOrDefault(RESETAUTOGEN, true);
      if (resetAutoGen) {
        parser.acceptsAll(Arrays.asList("r", "reset"), "Sets the command to its default value");
      }

      // set with constraints
      //set(defaultValue, false);
      this.value = defaultValue;
    } catch (Throwable t) {
      throw new CommandBuildException("Failed to build setting", t);
    }
  }

  public E get() {
    return value;
  }

  public E getMin() {
    return minValue;
  }

  public E getMax() {
    return maxValue;
  }

  public E getDefault() {
    return defaultValue;
  }

  @Nonnull
  public Class<?> getType() {
    return converter.type();
  }

  public boolean getAsBoolean() {
    return SafeConverter.toBoolean(get());
  }

  public byte getAsByte() {
    return SafeConverter.toByte(get());
  }

  public char getAsCharacter() {
    return SafeConverter.toCharacter(get());
  }

  public double getAsDouble() {
    return SafeConverter.toDouble(get());
  }

  public float getAsFloat() {
    return SafeConverter.toFloat(get());
  }

  public int getAsInteger() {
    return SafeConverter.toInteger(get());
  }

  public long getAsLong() {
    return SafeConverter.toLong(get());
  }

  public short getAsShort() {
    return SafeConverter.toShort(get());
  }

  public String getAsString() {
    return converter.toString(get());
  }

  public boolean set(E value, final boolean commandOutput) {
    if (comparator != null && value != null && this.value != null) {
      // clamp value to minimum and maximum value
      if (minValue != null && comparator.compare(value, minValue) < 0) {
        value = minValue;
      } else if (maxValue != null && comparator.compare(value, maxValue) > 0) {
        value = maxValue;
      }
    }

    if (!Objects.equals(get(), value)) {
      OnChangeCallback<E> cb = new OnChangeCallback<>(this, get(), value);
      invokeCallbacks(CallbackType.CHANGE, cb);
      if (cb.isCanceled()) {
        return false;
      }

      if (commandOutput) {
        String logMsg = String.format("%s = %s", getAbsoluteName(), converter.toStringSafe(value));
        ConsoleIO.write(logMsg); // Print for every other setting
      }

      this.value = value;
      return true;
    }
    return false;
  }

  public boolean set(E value) {
    return set(value, true);
  }

  public boolean rawSet(String value, boolean output) {
    return set(converter.parseSafe(value), output);
  }

  public boolean rawSet(String value) {
    return rawSet(value, true);
  }

  public boolean reset(boolean commandOutput) {
    return set(defaultValue, commandOutput);
  }

  public TypeConverter<E> getConverter() {
    return converter;
  }

  @Override
  public String getPrintText() {
    return getName() + " = " + getAsString() + " - " + getDescription();
  }

  @Override
  public boolean addChild(@Nonnull Command child) {
    throw new UnsupportedOperationException(
        "Command::addChild is not supported for a Setting type");
  }

  @Override
  public boolean removeChild(@Nonnull Command child) {
    return false;
  }

  @Nullable
  @Override
  public Command getChild(String name) {
    return null;
  }

  @Override
  public Collection<Command> getChildren() {
    return Collections.emptySet();
  }

  @Override
  public void getChildrenDeep(Collection<Command> all) {
  }

  @Override
  public Collection<Command> getChildrenDeep() {
    return Collections.emptySet();
  }

  @Override
  protected boolean preprocessor(String[] args) {
    if (args.length > 0) {
      String opt = args[0];
      if (opt.matches("-r|--reset")) {
        reset(true);
        serialize();
        return false;
      }
    }
    return true;
  }

  @Override
  public void serialize(JsonWriter writer) throws IOException {
    writer.beginObject();

    writer.name("value");
    writer.value(getAsString());

    writer.endObject();
  }

  @Override
  public void deserialize(JsonReader reader) throws IOException {
    reader.beginObject();

    reader.nextName(); // value
    rawSet(reader.nextString(), false);

    reader.endObject();
  }
}
