package com.matt.forgehax.util.command.v2;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.matt.forgehax.util.Immutables;
import com.matt.forgehax.util.command.v2.argument.ArgHelper;
import com.matt.forgehax.util.command.v2.argument.IArg;
import com.matt.forgehax.util.command.v2.argument.IOption;
import com.matt.forgehax.util.command.v2.callback.ICmdCallback;
import com.matt.forgehax.util.command.v2.exception.CmdExceptions;
import com.matt.forgehax.util.command.v2.exception.CmdRuntimeException;
import com.matt.forgehax.util.command.v2.flag.ICmdFlag;
import com.matt.forgehax.util.command.v2.serializers.CmdFlagSerializer;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Created on 12/26/2017 by fr1kin */
public abstract class AbstractCmd implements ICmd {
  private final String name;
  private final Collection<String> aliases;
  private final String description;
  private final IParentCmd parent;
  private final List<IArg<?>> arguments;
  private final List<IOption<?>> options;

  protected final Set<Enum<? extends ICmdFlag>> flags = Sets.newCopyOnWriteArraySet();
  protected final Set<ICmdCallback> callbacks = Sets.newCopyOnWriteArraySet();

  public AbstractCmd(
      String name,
      @Nullable Collection<String> aliases,
      String description,
      @Nullable IParentCmd parent,
      @Nullable Collection<IArg<?>> arguments,
      @Nullable Collection<IOption<?>> options)
      throws CmdRuntimeException.CreationFailure {
    CmdExceptions.checkIfNullOrEmpty(name, "name empty or null");
    CmdExceptions.checkIfNullOrEmpty(description, "description empty or null");

    this.name = name;
    this.aliases = Immutables.copyToList(aliases);
    this.description = description;
    this.parent = parent;
    this.arguments = Immutables.copyToList(arguments);
    this.options = Immutables.copyToList(options);

    // make sure no alias with the same name as the command exists
    if (this.aliases.stream().anyMatch(name::equalsIgnoreCase))
      throw new CmdRuntimeException.CreationFailure("alias cannot be root name");

    if (ArgHelper.isInvalidArgumentOrder(this.arguments))
      throw new CmdRuntimeException.CreationFailure(
          "required arguments are not allowed after non-required arguments");

    __initialize();
  }

  /** Method that is called at the end of the super constructor. Useful for adding flags. */
  protected void __initialize() {}

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getAbsoluteName() {
    return parent == null ? name : parent.getAbsoluteName() + "." + name;
  }

  @Nonnull
  @Override
  public Collection<String> getAliases() {
    return aliases;
  }

  @Override
  public boolean isIdentifiableAs(String name) {
    return CmdHelper.isNameValid(name)
        && (getName().equalsIgnoreCase(name)
            || getAbsoluteName().equalsIgnoreCase(name)
            || getAliases().stream().anyMatch(alias -> alias.equalsIgnoreCase(name)));
  }

  @Override
  public boolean isConflictingWith(ICmd other) {
    return isAbsoluteNameMatching(other.getAbsoluteName())
        || (!getAliases().isEmpty()
            && other
                .getAliases()
                .isEmpty() // we don't want to compute this is neither has aliases assigned
            && getAliases()
                .stream()
                .anyMatch(alias -> other.getAliases().stream().anyMatch(alias::equalsIgnoreCase)));
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Nullable
  @Override
  public IParentCmd getParent() {
    return parent;
  }

  @Override
  public List<IArg<?>> getArguments() {
    return arguments;
  }

  @Override
  public int getArgumentCount() {
    return arguments.size();
  }

  @Override
  public List<IOption<?>> getOptions() {
    return options;
  }

  @Override
  public String getHelpText(IHelpTextFormatter formatter) {
    return null; // TODO: finish
  }

  @Override
  public String getSyntaxHelpText() {
    return null; // TODO: finish
  }

  @Override
  public Collection<Enum<? extends ICmdFlag>> getFlags() {
    return ImmutableSet.copyOf(flags);
  }

  @Override
  public boolean addFlag(Enum<? extends ICmdFlag> flag) {
    return flags.add(flag);
  }

  @Override
  public boolean removeFlag(Enum<? extends ICmdFlag> flag) {
    return flags.remove(flag);
  }

  @Override
  public boolean containsFlag(Enum<? extends ICmdFlag> flag) {
    return !flags.isEmpty() && flags.contains(flag);
  }

  @Override
  public void clearFlags() {
    flags.clear();
  }

  @Nonnull
  @Override
  public Collection<ICmdCallback> getCallbacks() {
    return ImmutableSet.copyOf(callbacks);
  }

  @Nonnull
  @Override
  public <T extends ICmdCallback> Collection<T> getCallbacksOfType(Class<T> clazz) {
    return callbacks
        .stream()
        .filter(clazz::isInstance)
        .map(clazz::cast)
        .collect(Immutables.toImmutableSet());
  }

  @Override
  public boolean addCallback(ICmdCallback callback) {
    return callbacks.add(callback);
  }

  @Override
  public boolean removeCallback(ICmdCallback callback) {
    return callbacks.remove(callback);
  }

  @Override
  public int compareTo(ICmd other) {
    return other == null ? 1 : String.CASE_INSENSITIVE_ORDER.compare(getName(), other.getName());
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof ICmd && isAbsoluteNameMatching(((ICmd) obj).getAbsoluteName());
  }

  @SuppressWarnings("unchecked")
  @Override
  public void serialize(JsonWriter writer) throws IOException {
    writer.beginObject(); // {

    writer.name("flags");
    CmdFlagSerializer.getInstance().serialize(this, writer);

    writer.endObject(); // }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void deserialize(JsonReader reader) throws IOException {
    reader.beginObject(); // {

    while (reader.hasNext()) {
      switch (reader.nextName()) {
        case "flags":
          {
            CmdFlagSerializer.getInstance().deserialize(this, reader);
            break;
          }
        default:
          reader.skipValue();
      }
    }

    reader.endObject(); // }
  }
}
