package com.matt.forgehax.util.command.v2;

import com.google.common.collect.Lists;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.matt.forgehax.util.command.v2.argument.ArgumentV2;
import com.matt.forgehax.util.command.v2.argument.ArgumentV2Builder;
import com.matt.forgehax.util.command.v2.argument.OptionV2;
import com.matt.forgehax.util.command.v2.exception.CommandRuntimeExceptionV2;
import com.matt.forgehax.util.command.v2.flag.DefaultCommandFlagsV2;
import com.matt.forgehax.util.typeconverter.TypeConverters;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created on 12/26/2017 by fr1kin
 */
public class ParentCommandV2 extends AbstractCommandV2 implements IParentCommandV2 {
    private static final ArgumentV2<String> ARGUMENT_CHILD = new ArgumentV2Builder<String>()
            .description("child name")
            .converter(TypeConverters.STRING)
            .build();

    private final List<ICommandV2> children = Collections.synchronizedList(Lists.newArrayList());

    public ParentCommandV2(String name,
                           @Nullable Collection<String> aliases,
                           String description,
                           @Nullable IParentCommandV2 parent,
                           @Nullable Collection<OptionV2> options) throws CommandRuntimeExceptionV2.CreationFailure {
        super(name, aliases, description, parent, Lists.newArrayList(ARGUMENT_CHILD), options);

        addFlag(DefaultCommandFlagsV2.NOT_SERIALIZABLE);
    }

    @Override
    public Collection<ICommandV2> getChildren() {
        synchronized (children) {
            return Lists.newArrayList(children);
        }
    }

    @Override
    public Collection<ICommandV2> getChildrenDeep() {
        synchronized (children) {
            Collection<ICommandV2> deep = getChildren();
            deep.stream()
                    .filter(IParentCommandV2.class::isInstance)
                    .map(IParentCommandV2.class::cast)
                    .forEach(parent -> deep.addAll(parent.getChildrenDeep()));
            return deep;
        }
    }

    @Override
    public boolean addChild(ICommandV2 command) {
        synchronized (children) {
            return children.add(command);
        }
    }

    @Override
    public boolean removeChild(ICommandV2 command) {
        synchronized (children) {
            return children.remove(command);
        }
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
        List<ICommandV2> matches;

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
}
