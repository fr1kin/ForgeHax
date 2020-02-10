package dev.fiki.forgehax.main.util.cmd.settings;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.cmd.*;
import dev.fiki.forgehax.main.util.cmd.argument.ConverterArgument;
import dev.fiki.forgehax.main.util.cmd.argument.IArgument;
import dev.fiki.forgehax.main.util.cmd.flag.EnumFlag;
import dev.fiki.forgehax.main.util.cmd.listener.ICommandListener;
import dev.fiki.forgehax.main.util.cmd.execution.ArgumentList;
import dev.fiki.forgehax.main.util.key.BindingHelper;
import dev.fiki.forgehax.main.util.reflection.FastReflection;
import dev.fiki.forgehax.main.util.serialization.IJsonSerializable;
import dev.fiki.forgehax.main.util.typeconverter.TypeConverters;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;

import java.util.*;

import static net.minecraft.client.util.InputMappings.*;

public class KeyBindingSetting extends AbstractCommand implements ISetting<Input>, IJsonSerializable {
  @Getter
  private static final List<KeyBindingSetting> registry = Lists.newCopyOnWriteArrayList();

  @Getter
  private KeyBinding keyBinding;

  private Multimap<Class<? extends ICommandListener>, ICommandListener> listeners;

  @Builder
  public KeyBindingSetting(IParentCommand parent,
      String name, @Singular Set<String> aliases, String description,
      @Singular Set<EnumFlag> flags,
      @NonNull String keyName, Input keyInput, @NonNull String keyCategory,
      @Singular List<ISettingValueChanged<Input>> changedListeners,
      @Singular List<IKeyDownListener> keyDownListeners,
      @Singular List<IKeyPressedListener> keyPressedListeners,
      @Singular List<IKeyReleasedListener> keyReleasedListeners) {
    super(parent, name, aliases, description, flags);
    addListeners(ISettingValueChanged.class, changedListeners);
    addListeners(IKeyDownListener.class, keyDownListeners);
    addListeners(IKeyPressedListener.class, keyPressedListeners);
    addListeners(IKeyReleasedListener.class, keyReleasedListeners);
    registry.add(this);

    // do this on the main thread
    Common.addScheduledTask(() -> {
      this.keyBinding = new KeyBinding(keyName,
          MoreObjects.firstNonNull(keyInput, INPUT_INVALID).getKeyCode(),
          keyCategory);

      BindingHelper.addBinding(this.keyBinding);
    });
  }

  public Input getKeyInput() {
    return getKeyBinding().getKey();
  }

  public void setKeyInput(Input input) {
    Objects.requireNonNull(input, "missing input");
    getKeyBinding().bind(input);
  }

  public int getKeyCode() {
    return getKeyInput().getKeyCode();
  }

  public void setKeyCode(int keyCode) {
    setKeyInput(BindingHelper.getInputByKeyCode(keyCode));
  }

  public Input getDefaultKeyInput() {
    return getKeyBinding().getDefault();
  }

  public void setKeyInvalid() {
    setKeyInput(INPUT_INVALID);
  }

  public boolean isKeyDown() {
    return FastReflection.Fields.KeyBinding_pressed.get(getKeyBinding());
  }

  public boolean isPressed() {
    return getKeyBinding().isPressed();
  }

  @Override
  protected void init() {
    this.listeners = Multimaps.newListMultimap(Maps.newConcurrentMap(), Lists::newCopyOnWriteArrayList);
  }

  @Override
  public Input getValue() {
    return getKeyInput();
  }

  @Override
  public boolean setValue(Input value) {
    if(!Objects.equals(getValue(), value)) {
      final Input newValue = value;
      final Input oldValue = getValue();
      getKeyBinding().bind(value);
      invokeListeners(ISettingValueChanged.class, l -> l.onValueChanged(oldValue, newValue));
      callUpdateListeners();
      return true;
    }
    return false;
  }

  @Override
  public boolean setValueRaw(String value) {
    return setValue(TypeConverters.INPUT.parse(value));
  }

  @Override
  public Input getDefaultValue() {
    return getKeyBinding().getDefault();
  }

  @Override
  public Input getMinValue() {
    return null;
  }

  @Override
  public Input getMaxValue() {
    return null;
  }

  @Override
  public List<IArgument<?>> getArguments() {
    return Collections.singletonList(ConverterArgument.<Input>builder()
        .converter(TypeConverters.INPUT)
        .defaultValue(getDefaultValue())
        .minArgumentsConsumed(1)
        .maxArgumentsConsumed(1)
        .build());
  }

  @Override
  public ICommand onExecute(ArgumentList args) {
    if(setValue(args.<Input>getFirst().getValue())) {
      args.inform("%s = %s", getFullName(), args.<Input>getFirst().getStringValue());
    }
    return null;
  }

  @Override
  public boolean addListeners(Class<? extends ICommandListener> type, Collection<? extends ICommandListener> listener) {
    return type != null
        && listener != null
        && listeners.putAll(type, listener);
  }

  @Override
  public <T extends ICommandListener> List<T> getListeners(Class<T> type) {
    return (List<T>) listeners.get(type);
  }

  @Override
  public JsonElement serialize() {
    return new JsonPrimitive(TypeConverters.INPUT.convert(getValue()));
  }

  @Override
  public void deserialize(JsonElement json) {
    if(json.isJsonPrimitive()) {
      getKeyBinding().bind(TypeConverters.INPUT.parse(json.getAsString()));
    } else {
      throw new IllegalArgumentException("expected JsonPrimitive, got " + json.getClass().getSimpleName());
    }
  }

  @Override
  public String toString() {
    return getName() + " = " + TypeConverters.INPUT.convert(getValue());
  }

  public interface IKeyDownListener extends ICommandListener {
    void onKeyDown(KeyBinding key);
  }

  public interface IKeyPressedListener extends ICommandListener {
    void onKeyPressed(KeyBinding key);
  }

  public interface IKeyReleasedListener extends ICommandListener {
    void onKeyReleased(KeyBinding key);
  }

  public static class KeyBindingSettingBuilder {
    public KeyBindingSettingBuilder keyCode(int keyCode) {
      return keyInput(BindingHelper.getInputByKeyCode(keyCode));
    }

    public KeyBindingSettingBuilder key(String key) {
      return keyInput(BindingHelper.getInputByName(key));
    }
  }
}
