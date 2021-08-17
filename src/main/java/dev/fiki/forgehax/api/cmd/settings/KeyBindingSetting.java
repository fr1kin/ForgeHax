package dev.fiki.forgehax.api.cmd.settings;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.fiki.forgehax.api.cmd.AbstractCommand;
import dev.fiki.forgehax.api.cmd.ICommand;
import dev.fiki.forgehax.api.cmd.IParentCommand;
import dev.fiki.forgehax.api.cmd.ISetting;
import dev.fiki.forgehax.api.cmd.argument.ConverterArgument;
import dev.fiki.forgehax.api.cmd.argument.IArgument;
import dev.fiki.forgehax.api.cmd.execution.ArgumentList;
import dev.fiki.forgehax.api.cmd.flag.EnumFlag;
import dev.fiki.forgehax.api.cmd.listener.ICommandListener;
import dev.fiki.forgehax.api.key.BindingHelper;
import dev.fiki.forgehax.api.key.KeyBindingEx;
import dev.fiki.forgehax.api.key.KeyInput;
import dev.fiki.forgehax.api.key.KeyInputs;
import dev.fiki.forgehax.api.serialization.IJsonSerializable;
import dev.fiki.forgehax.api.typeconverter.TypeConverters;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.IKeyConflictContext;

import java.util.*;

import static dev.fiki.forgehax.main.Common.addScheduledTask;
import static net.minecraft.client.util.InputMappings.*;

public final class KeyBindingSetting extends AbstractCommand implements ISetting<KeyInput>, IJsonSerializable {
  @Getter
  private static final List<KeyBindingSetting> registry = Lists.newCopyOnWriteArrayList();

  private final Multimap<Class<? extends ICommandListener>, ICommandListener> listeners =
      Multimaps.newListMultimap(Maps.newConcurrentMap(), Lists::newCopyOnWriteArrayList);

  @Getter
  private KeyBindingEx keyBinding;

  @Builder
  public KeyBindingSetting(IParentCommand parent,
      String name, @Singular Set<String> aliases, String description,
      @Singular Set<EnumFlag> flags,
      @NonNull String keyName, @NonNull KeyInput key, @NonNull String keyCategory,
      IKeyConflictContext conflictContext,
      @Singular List<ISettingValueChanged<KeyInput>> changedListeners,
      @Singular List<IKeyDownListener> keyDownListeners,
      @Singular List<IKeyPressedListener> keyPressedListeners,
      @Singular List<IKeyReleasedListener> keyReleasedListeners) {
    super(parent, name, aliases, description, flags);

    // register all listeners
    addListeners(ISettingValueChanged.class, changedListeners);
    addListeners(IKeyDownListener.class, keyDownListeners);
    addListeners(IKeyPressedListener.class, keyPressedListeners);
    addListeners(IKeyReleasedListener.class, keyReleasedListeners);

    // all listeners should be called on the main thread
    addFlag(EnumFlag.EXECUTOR_MAIN_THREAD);

    // add this to the list of registered keybind settings
    registry.add(this);

    // KeyBinding will mutate non-thread safe maps and collections so all
    // code must be ran on the main thread
    addScheduledTask(() -> {
      this.keyBinding = KeyBindingEx.builder()
          .type(key.getType())
          .description(keyName)
          .category(keyCategory)
          .keyCode(key.getCode())
          .conflictContext(conflictContext)
          .changeCallback(this::onExternalChanged)
          .build();

      BindingHelper.addBinding(this.keyBinding);
    });

    onFullyConstructed();
  }

  private void onExternalChanged(Input value) {
    addScheduledTask(() -> bind(value));
  }

  public Type getType() {
    return getValue().getType();
  }

  public KeyInput getKeyInput() {
    return KeyInput.getKeyInputByCode(getKeyCode());
  }

  private boolean bind(final Input value) {
    final Input currentValue = getKeyInput().getInputMapping();
    if(!Objects.equals(currentValue, value)) {
      getKeyBinding().setBind(value);
      BindingHelper.updateKeyBindings();
      BindingHelper.saveGameSettings();
      invokeListeners(ISettingValueChanged.class, l -> l.onValueChanged(currentValue, value));
      callUpdateListeners();
      return true;
    }
    return false;
  }

  public void bind(int keyCode) {
    bind(KeyInput.getKeyInputByCode(keyCode).getInputMapping());
  }

  public void unbind() {
    bind(UNKNOWN);
  }

  public int getKeyCode() {
    return getKeyBinding().getKey().getValue();
  }

  public boolean isKeyDown() {
    return getKeyBinding().isDown();
  }

  public boolean isKeyDownUnchecked() {
    return getKeyBinding().isKeyDownUnchecked();
  }

  public boolean isPressed() {
    return getKeyBinding().consumeClick();
  }

  public boolean isUnbound() {
    return UNKNOWN.equals(getKeyBinding().getKey());
  }

  public int getKeyPressedTime() {
    return getKeyBinding().getKeyPressedTime();
  }

  public String getKeyName() {
    return getKeyInput().getName();
  }

  @Override
  public KeyInput getValue() {
    return KeyInput.getKeyInputByCode(getKeyBinding().getKey().getValue());
  }

  @Override
  public boolean setValue(KeyInput value) {
    return bind(value.getInputMapping());
  }

  @Override
  public boolean setValueRaw(String value) {
    return setValue(TypeConverters.KEY_INPUT.parse(value));
  }

  @Override
  public KeyInput getDefaultValue() {
    return KeyInput.getKeyInputByCode(getKeyBinding().getKey().getValue());
  }

  @Override
  public KeyInput getMinValue() {
    return null;
  }

  @Override
  public KeyInput getMaxValue() {
    return null;
  }

  @Override
  public List<IArgument<?>> getArguments() {
    return Collections.singletonList(ConverterArgument.<KeyInput>builder()
        .converter(TypeConverters.KEY_INPUT)
        .defaultValue(getDefaultValue())
        .minArgumentsConsumed(1)
        .maxArgumentsConsumed(1)
        .build());
  }

  @Override
  public ICommand onExecute(ArgumentList args) {
    if(setValue(args.<KeyInput>getFirst().getValue())) {
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
    return new JsonPrimitive(TypeConverters.KEY_INPUT.convert(getValue()));
  }

  @Override
  public void deserialize(JsonElement json) {
    if(json.isJsonPrimitive()) {
      Input input = Objects.requireNonNull(TypeConverters.KEY_INPUT.parse(json.getAsString()), "key input")
          .getInputMapping();
      Objects.requireNonNull(input, "input");
      // dont use our ::bind method because we don't want to call the listeners
      // use addScheduledTask because ::getKeyBinding may be null still
      addScheduledTask(() -> Objects.requireNonNull(getKeyBinding(), "keyBinding still null").setBind(input));
    } else {
      throw new IllegalArgumentException("expected JsonPrimitive, got " + json.getClass().getSimpleName());
    }
  }

  @Override
  public String toString() {
    return getName() + " = " + getKeyName();
  }

  public interface IKeyDownListener extends ICommandListener {
    void onKeyDown(KeyBinding key);

    @Override
    default Class<? extends ICommandListener> getListenerClassType() {
      return IKeyDownListener.class;
    }
  }

  public interface IKeyPressedListener extends ICommandListener {
    void onKeyPressed(KeyBinding key);

    @Override
    default Class<? extends ICommandListener> getListenerClassType() {
      return IKeyPressedListener.class;
    }
  }

  public interface IKeyReleasedListener extends ICommandListener {
    void onKeyReleased(KeyBinding key);

    @Override
    default Class<? extends ICommandListener> getListenerClassType() {
      return IKeyReleasedListener.class;
    }
  }

  public static class KeyBindingSettingBuilder {
    public KeyBindingSettingBuilder keyInputByCode(int keyCode) {
      return key(KeyInput.getKeyInputByCode(keyCode));
    }

    public KeyBindingSettingBuilder keyInputByName(String key) {
      return key(KeyInput.getKeyInputByName(key));
    }

    public KeyBindingSettingBuilder defaultKeyCategory() {
      return keyCategory("ForgeHax");
    }

    public KeyBindingSettingBuilder keyName(String name) {
      this.keyName = (parent == null
          ? name
          : (parent.getName() + " " + name)).trim();
      return this;
    }

    public KeyBindingSettingBuilder defaultKeyName() {
      return keyName("");
    }

    public KeyBindingSettingBuilder unbound() {
      return key(KeyInputs.UNBOUND);
    }
  }
}
