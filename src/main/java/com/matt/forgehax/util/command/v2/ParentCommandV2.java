package com.matt.forgehax.util.command.v2;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.matt.forgehax.util.command.v2.argument.ArgumentV2;
import com.matt.forgehax.util.command.v2.argument.ArgumentV2Builder;
import com.matt.forgehax.util.command.v2.argument.OptionV2;
import com.matt.forgehax.util.command.v2.exception.CommandRuntimeExceptionV2;
import com.matt.forgehax.util.command.v2.flag.ICommandFlagV2;
import com.matt.forgehax.util.typeconverter.TypeConverters;
import joptsimple.internal.Strings;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created on 12/26/2017 by fr1kin
 */
public class ParentCommandV2 extends AbstractCommandV2 implements IParentCommandV2 {
    private static final List<ArgumentV2<?>> ARGUMENTS = Lists.newArrayList(new ArgumentV2Builder<String>()
            .description("child command")
            .converter(TypeConverters.STRING)
            .required()
            .interpreter(((caller, command, input) -> {
                ParentCommandV2 cmd = (ParentCommandV2)command;
                if(Strings.isNullOrEmpty(input))
                    return cmd.children.stream()
                            .map(ICommandV2::getName)
                            .sorted(String.CASE_INSENSITIVE_ORDER)
                            .collect(Collectors.toList());
                else
                    return cmd.children.stream()
                            .map(c -> CommandHelperV2.getBestMatchingName(c, input))
                            .filter(Objects::nonNull)
                            .sorted(String.CASE_INSENSITIVE_ORDER)
                            .collect(Collectors.toList());
            }))
            .build());

    private final List<ICommandV2> children = Lists.newCopyOnWriteArrayList();

    public ParentCommandV2(String name,
                           @Nullable Collection<String> aliases,
                           String description,
                           @Nullable IParentCommandV2 parent,
                           @Nullable Collection<OptionV2<?>> options) throws CommandRuntimeExceptionV2.CreationFailure {
        super(name, aliases, description, parent, ARGUMENTS, options);
    }

    protected void checkConflictingChildren(ICommandV2 command) {
        if(children.stream().anyMatch(command::isConflictingWith)) throw new CommandRuntimeExceptionV2("tried to add command that conflicts with another child");
    }

    @Override
    public Collection<ICommandV2> getChildren() {
        return ImmutableList.copyOf(children);
    }

    @Override
    public Collection<ICommandV2> getChildrenDeep() {
        Collection<ICommandV2> deep = getChildren();
        deep.stream()
                .filter(IParentCommandV2.class::isInstance)
                .map(IParentCommandV2.class::cast)
                .forEach(parent -> deep.addAll(parent.getChildrenDeep()));
        return deep;
    }

    @Override
    public boolean addChild(ICommandV2 command) {
        checkConflictingChildren(command);
        return children.add(command);
    }

    @Override
    public boolean removeChild(ICommandV2 command) {
        return children.remove(command);
    }

    @Override
    public CommandBuilderV2 makeChild() {
        return new CommandBuilderV2(this);
    }

    public ICommandV2 findChild(final String name) {
        return children.stream()
                .filter(cmd -> cmd.isNameMatching(name) || cmd.isAbsoluteNameMatching(name))
                .findFirst()
                .orElse(null);
    }

    @Override
    public CommandBuilderV2 copy(IParentCommandV2 parent) {
        return new CommandBuilderV2(parent)
                .name(getName())
                .description(getDescription())
                .aliases(getAliases());
    }

    @Override
    public boolean process(String[] args) throws CommandRuntimeExceptionV2.ProcessingFailure {
        final String next = args[0];
        //getArguments().get(0).getInterpretations(this, next);

        return false;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void serialize(JsonWriter writer) throws IOException {
        writer.beginObject(); // {

        // start with core, always write this

        if(!flags.isEmpty()) {
            writer.name("flags");
            writer.beginArray(); // [

            for(Enum<? extends ICommandFlagV2> val : flags) {
                writer.value(val.getDeclaringClass().getName() + "::" + val.name());
            }

            writer.endArray(); // ]
        }

        if (!children.isEmpty()) { // only process if not empty
            writer.name("children");
            writer.beginObject(); // {

            for (ICommandV2 cmd : children) {
                writer.name(cmd.getUniqueHeader());
                cmd.serialize(writer);
            }

            writer.endObject(); // }
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
                case "children":
                {
                    reader.beginObject(); // {

                    while (reader.hasNext()) {
                        String name = reader.nextName(); // name is by default the absolute name
                        ICommandV2 cmd = findChild(name);
                        if(cmd != null)
                            cmd.deserialize(reader);
                        else
                            reader.skipValue(); // missing command, skip
                    }

                    reader.endObject(); // }
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
