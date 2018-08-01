package com.matt.forgehax.util.command.v2;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.matt.forgehax.util.Immutables;
import com.matt.forgehax.util.command.v2.argument.ArgumentHelper;
import com.matt.forgehax.util.command.v2.argument.ArgumentV2;
import com.matt.forgehax.util.command.v2.argument.OptionV2;
import com.matt.forgehax.util.command.v2.callback.ICommandCallbackV2;
import com.matt.forgehax.util.command.v2.exception.CommandExceptions;
import com.matt.forgehax.util.command.v2.exception.CommandRuntimeExceptionV2;
import com.matt.forgehax.util.command.v2.flag.ICommandFlagV2;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created on 12/26/2017 by fr1kin
 */
public abstract class AbstractCommandV2 implements ICommandV2 {
    private final String name;
    private final Collection<String> aliases;
    private final String description;
    private final IParentCommandV2 parent;
    private final List<ArgumentV2<?>> arguments;
    private final List<OptionV2<?>> options;

    protected final Set<Enum<? extends ICommandFlagV2>> flags = Sets.newCopyOnWriteArraySet();
    protected final Set<ICommandCallbackV2> callbacks = Sets.newCopyOnWriteArraySet();

    public AbstractCommandV2(String name,
                             @Nullable Collection<String> aliases,
                             String description,
                             @Nullable IParentCommandV2 parent,
                             @Nullable Collection<ArgumentV2<?>> arguments,
                             @Nullable Collection<OptionV2<?>> options) throws CommandRuntimeExceptionV2.CreationFailure {
        CommandExceptions.checkIfNullOrEmpty(name, "name empty or null");
        CommandExceptions.checkIfNullOrEmpty(description, "description empty or null");

        this.name = name;
        this.aliases = Immutables.copyToList(aliases);
        this.description = description;
        this.parent = parent;
        this.arguments = Immutables.copyToList(arguments);
        this.options = Immutables.copyToList(options);

        // make sure no alias with the same name as the command exists
        if(this.aliases.stream().anyMatch(name::equalsIgnoreCase))
            throw new CommandRuntimeExceptionV2.CreationFailure("alias cannot be root name");

        if(ArgumentHelper.isInvalidArgumentOrder(this.arguments))
            throw new CommandRuntimeExceptionV2.CreationFailure("required arguments are not allowed after non-required arguments");

        __initialize();
    }

    /**
     * Method that is called at the end of the super constructor.
     * Useful for adding flags.
     */
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
        return CommandHelperV2.isNameValid(name) && (
                getName().equalsIgnoreCase(name)
                        || getAbsoluteName().equalsIgnoreCase(name)
                        || getAliases().stream().anyMatch(alias -> alias.equalsIgnoreCase(name)));
    }

    @Override
    public boolean isConflictingWith(ICommandV2 other) {
        return getName().equalsIgnoreCase(other.getName()) || (
                !getAliases().isEmpty() && other.getAliases().isEmpty() // we don't want to compute this is neither has aliases assigned
                        && getAliases().stream().anyMatch(alias -> other.getAliases().stream().anyMatch(alias::equalsIgnoreCase)));
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Nullable
    @Override
    public IParentCommandV2 getParent() {
        return parent;
    }

    @Override
    public List<ArgumentV2<?>> getArguments() {
        return arguments;
    }

    @Override
    public List<OptionV2<?>> getOptions() {
        return options;
    }

    @Override
    public String getHelpText(IHelpTextFormatter formatter) {
        return null; //TODO: finish
    }

    @Override
    public String getSyntaxHelpText() {
        return null; //TODO: finish
    }

    @Override
    public Collection<Enum<? extends ICommandFlagV2>> getFlags() {
        return ImmutableSet.copyOf(flags);
    }

    @Override
    public boolean addFlag(Enum<? extends ICommandFlagV2> flag) {
        return flags.add(flag);
    }

    @Override
    public boolean removeFlag(Enum<? extends ICommandFlagV2> flag) {
        return flags.remove(flag);
    }

    @Override
    public boolean containsFlag(Enum<? extends ICommandFlagV2> flag) {
        return !flags.isEmpty() && flags.contains(flag);
    }

    @Nonnull
    @Override
    public Collection<ICommandCallbackV2> getCallbacks() {
        return ImmutableSet.copyOf(callbacks);
    }

    @Nonnull
    @Override
    public <T extends ICommandCallbackV2> Collection<T> getCallbacksOfType(Class<T> clazz) {
        return callbacks.stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .collect(Immutables.toImmutableSet());
    }

    @Override
    public boolean addCallback(ICommandCallbackV2 callback) {
        return callbacks.add(callback);
    }

    @Override
    public boolean removeCallback(ICommandCallbackV2 callback) {
        return callbacks.remove(callback);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ICommandV2 && isAbsoluteNameMatching(((ICommandV2) obj).getAbsoluteName());
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void serialize(JsonWriter writer) throws IOException {
        writer.beginObject(); // {

        // core attributes

        if(!flags.isEmpty()) { // only write if not-empty
            writer.name("flags");
            writer.beginArray(); // [

            for(Enum<? extends ICommandFlagV2> val : flags) {
                writer.value(val.getDeclaringClass().getName() + "::" + val.name());
            }

            writer.endArray(); // ]
        }

        writer.endObject(); // }
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void deserialize(JsonReader reader) throws IOException {
        reader.beginObject(); // {

        while(reader.hasNext()) {
            switch (reader.nextName()) {
                case "flags": // should always be first
                {
                    reader.beginArray(); // [

                    flags.clear(); // remove all current flags

                    while(reader.hasNext()) {
                        String next = reader.nextName();
                        Enum<? extends ICommandFlagV2> value = ICommandFlagV2.Registry.getFromSerializedString(next);
                        if(value != null) addFlag(value);
                    }

                    reader.endArray(); // ]

                    break;
                }
                default:
                {
                    // possibly legacy code
                    reader.skipValue();
                    break;
                }
            }
        }

        reader.endObject(); // }
    }
}
