package dev.fiki.forgehax.api.cmd;

import dev.fiki.forgehax.api.cmd.argument.Arguments;
import dev.fiki.forgehax.api.cmd.settings.*;
import dev.fiki.forgehax.api.cmd.settings.collections.CustomSettingList;
import dev.fiki.forgehax.api.cmd.settings.collections.CustomSettingSet;
import dev.fiki.forgehax.api.cmd.settings.collections.SimpleSettingList;
import dev.fiki.forgehax.api.cmd.settings.collections.SimpleSettingSet;
import dev.fiki.forgehax.api.cmd.settings.maps.SimpleSettingMap;
import dev.fiki.forgehax.api.serialization.IJsonSerializable;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

public interface IParentCommand extends ICommand {
  Collection<ICommand> getChildren();

  ICommand getChildByName(String command);
  List<ICommand> getPossibleMatchingChildren(String search);

  boolean addChild(ICommand command);
  boolean deleteChild(ICommand command);
  boolean containsChild(ICommand command);

  default SimpleCommand.SimpleCommandBuilder newSimpleCommand() {
    return SimpleCommand.builder().parent(this);
  }

  default BooleanSetting.BooleanSettingBuilder newBooleanSetting() {
    return BooleanSetting.builder().parent(this);
  }

  default ByteSetting.ByteSettingBuilder newByteSetting() {
    return ByteSetting.builder().parent(this);
  }

  default CharacterSetting.CharacterSettingBuilder newCharacterSetting() {
    return CharacterSetting.builder().parent(this);
  }

  default DoubleSetting.DoubleSettingBuilder newDoubleSetting() {
    return DoubleSetting.builder().parent(this);
  }

  default FloatSetting.FloatSettingBuilder newFloatSetting() {
    return FloatSetting.builder().parent(this);
  }

  default IntegerSetting.IntegerSettingBuilder newIntegerSetting() {
    return IntegerSetting.builder().parent(this);
  }

  default LongSetting.LongSettingBuilder newLongSetting() {
    return LongSetting.builder().parent(this);
  }

  default ShortSetting.ShortSettingBuilder newShortSetting() {
    return ShortSetting.builder().parent(this);
  }

  default StringSetting.StringSettingBuilder newStringSetting() {
    return StringSetting.builder().parent(this);
  }

  default CaseInsensitiveString.CaseInsensitiveStringBuilder newCaseInsensitiveStringSetting() {
    return CaseInsensitiveString.builder().parent(this);
  }

  default KeyBindingSetting.KeyBindingSettingBuilder newKeyBindingSetting() {
    return KeyBindingSetting.builder().parent(this);
  }

  default ColorSetting.ColorSettingBuilder newColorSetting() {
    return ColorSetting.builder().parent(this);
  }

  default PatternSetting.PatternSettingBuilder newPatternSetting() {
    return PatternSetting.builder().parent(this);
  }

  default <T extends Enum<T>> EnumSetting.EnumSettingBuilder<T> newEnumSetting() {
    return EnumSetting.<T>builder().parent(this);
  }

  default <T extends Enum<T>> EnumSetting.EnumSettingBuilder<T> newEnumSetting(Class<T> classContext) {
    return EnumSetting.<T>builder().parent(this);
  }

  default <T> SimpleSettingList.SimpleSettingListBuilder<T> newSimpleSettingList() {
    return SimpleSettingList.<T>builder().parent(this);
  }

  default <T> SimpleSettingList.SimpleSettingListBuilder<T> newSimpleSettingList(Class<T> ctxClass) {
    return SimpleSettingList.<T>builder().parent(this);
  }

  default <T> SimpleSettingSet.SimpleSettingSetBuilder<T> newSimpleSettingSet() {
    return SimpleSettingSet.<T>builder().parent(this);
  }

  default <T> SimpleSettingSet.SimpleSettingSetBuilder<T> newSimpleSettingSet(Class<T> ctxClass) {
    return SimpleSettingSet.<T>builder().parent(this);
  }

  default <T extends Enum<T>> SimpleSettingSet.SimpleSettingSetBuilder<T> newSimpleSettingEnumSet(Class<T> ctxClass) {
    return SimpleSettingSet.<T>builder().parent(this)
        .argument(Arguments.newEnumArgument(ctxClass)
            .label("enum")
            .build())
        .supplier(() -> EnumSet.noneOf(ctxClass));
  }

  default <T extends IJsonSerializable> CustomSettingList.CustomSettingListBuilder<T> newCustomSettingList() {
    return CustomSettingList.<T>builder().parent(this);
  }

  default <T extends IJsonSerializable> CustomSettingList.CustomSettingListBuilder<T> newCustomSettingList(Class<T> ctxClass) {
    return CustomSettingList.<T>builder().parent(this);
  }

  default <T extends IJsonSerializable> CustomSettingSet.CustomSettingSetBuilder<T> newCustomSettingSet() {
    return CustomSettingSet.<T>builder().parent(this);
  }

  default <T extends IJsonSerializable> CustomSettingSet.CustomSettingSetBuilder<T> newCustomSettingSet(Class<T> ctxClass) {
    return CustomSettingSet.<T>builder().parent(this);
  }

  default <K, V> SimpleSettingMap.SimpleSettingMapBuilder<K, V> newSettingMap() {
    return SimpleSettingMap.<K, V>builder().parent(this);
  }

  default <K, V> SimpleSettingMap.SimpleSettingMapBuilder<K, V> newSettingMap(Class<K> ctxKey, Class<V> ctxValue) {
    return SimpleSettingMap.<K, V>builder().parent(this);
  }
}
