package com.matt.forgehax.util.command.v2;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.matt.forgehax.util.command.v2.argument.ArgumentV2;
import com.matt.forgehax.util.command.v2.argument.ArgumentV2Builder;
import com.matt.forgehax.util.command.v2.argument.OptionV2;
import com.matt.forgehax.util.command.v2.exception.CommandRuntimeExceptionV2;
import com.matt.forgehax.util.command.v2.flag.DefaultCommandFlagsV2;
import com.matt.forgehax.util.typeconverter.TypeConverters;
import joptsimple.internal.Strings;

import javax.annotation.Nonnull;
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
    private final List<ICommandV2> children = Lists.newCopyOnWriteArrayList();

    public ParentCommandV2(String name,
                           @Nullable Collection<String> aliases,
                           String description,
                           @Nullable IParentCommandV2 parent,
                           @Nullable Collection<OptionV2> options) throws CommandRuntimeExceptionV2.CreationFailure {
        super(name, aliases, description, parent,
                Lists.newArrayList(new ArgumentV2Builder<String>()
                        .description("child command")
                        .converter(TypeConverters.STRING)
                        .required(true)
                        .predictor(ParentCommandV2::predictor)
                        .build()),
                options);

        addFlag(DefaultCommandFlagsV2.NOT_SERIALIZABLE);
    }

    private void checkConflictingChildren(ICommandV2 command) {
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
        //getArguments().get(0).getPredictions(this, next);

        return false;
    }

    @Override
    public void serialize(JsonWriter writer) throws IOException {
        throw new UnsupportedOperationException("Attempted to serialize a non-serializable command");
    }

    @Override
    public void deserialize(JsonReader reader) throws IOException {
        throw new UnsupportedOperationException("Attempted to deserialize a non-serializable command");
    }

    //
    //
    //

    @Nonnull
    private static List<String> predictor(ArgumentV2<String> argument, ICommandV2 o, String input) {
        ParentCommandV2 command = ParentCommandV2.class.cast(o);
        if(Strings.isNullOrEmpty(input))
            return command.children.stream()
                    .map(ICommandV2::getName)
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .collect(Collectors.toList());
        else
            return command.children.stream()
                    .map(cmd -> CommandHelperV2.getBestMatchingName(command, input))
                    .filter(Objects::nonNull)
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .collect(Collectors.toList());
    }
}
