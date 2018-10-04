package com.matt.forgehax.util.command;

import com.matt.forgehax.util.serialization.ISerializableJson;

/** Created on 6/3/2017 by fr1kin */
public class CommandBuilders {
  private static final CommandBuilders INSTANCE = new CommandBuilders();

  public static CommandBuilders getInstance() {
    return INSTANCE;
  }

  public static CommandBuilders newInstance(Command parent) {
    return new CommandBuilders(parent);
  }

  private final Command parent;

  public CommandBuilders(Command parent) {
    this.parent = parent;
  }

  public CommandBuilders() {
    this(null);
  }

  public CommandBuilder newCommandBuilder() {
    return new CommandBuilder().parent(parent);
  }

  public StubBuilder newStubBuilder() {
    return new StubBuilder().parent(parent);
  }

  public <T> SettingBuilder<T> newSettingBuilder() {
    return new SettingBuilder<T>().parent(parent);
  }

  public <T extends Enum<T>> SettingEnumBuilder<T> newSettingEnumBuilder() {
    return new SettingEnumBuilder<T>().parent(parent);
  }

  public <T extends ISerializableJson> OptionsBuilder<T> newOptionsBuilder() {
    return new OptionsBuilder<T>().parent(parent);
  }
}
