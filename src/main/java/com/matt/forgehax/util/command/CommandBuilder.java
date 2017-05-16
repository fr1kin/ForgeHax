package com.matt.forgehax.util.command;

import com.google.common.collect.Sets;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.minecraftforge.common.config.Property;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created on 5/14/2017 by fr1kin
 */
public class CommandBuilder {
    public static CommandBuilder create() {
        return new CommandBuilder();
    }

    private String name;
    private String description;

    private Consumer<OptionParser> optionBuilder;
    private Function<OptionSet, Boolean> processor;

    private Set<Consumer<Command>> callbacks = Sets.newHashSet();

    private Property property;

    private boolean autoHelpText = true;

    public CommandBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public CommandBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public CommandBuilder setProcessor(Function<OptionSet, Boolean> processor) {
        this.processor = processor;
        return this;
    }

    public CommandBuilder setOptionBuilder(Consumer<OptionParser> optionBuilder) {
        this.optionBuilder = optionBuilder;
        return this;
    }

    public CommandBuilder setProperty(Property property) {
        this.property = property;
        return this;
    }

    public void setAutoHelpText(boolean autoHelpText) {
        this.autoHelpText = autoHelpText;
    }

    public CommandBuilder addCallback(Consumer<Command> consumer) {
        callbacks.add(consumer);
        return this;
    }

    public Command build() {
        if(property == null) return new Command(name, description, optionBuilder, processor, callbacks, autoHelpText);
        else return new CommandWithProperty(property, optionBuilder, processor, callbacks, autoHelpText);
    }
}
