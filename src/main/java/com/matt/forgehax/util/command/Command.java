package com.matt.forgehax.util.command;

import com.google.common.collect.Sets;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created on 5/14/2017 by fr1kin
 */
public class Command {
    private final String name;
    private final String description;

    protected final OptionParser parser = new OptionParser();
    protected final Function<OptionSet, Boolean> processor;

    protected final Set<Consumer<Command>> callbacks = Sets.newHashSet();

    public Command(String name, String description, Consumer<OptionParser> buildParser, Function<OptionSet, Boolean> processor, Collection<Consumer<Command>> callbacks) {
        this.name = name;
        this.description = description;
        this.processor = processor;
        if(callbacks != null) this.callbacks.addAll(callbacks);
        if(buildParser != null) buildParser.accept(parser);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void run(String[] args) {
        if(processor != null) {
            Objects.requireNonNull(args, "args[] is null");
            OptionSet options = parser.parse(args);
            try {
                if(processor.apply(options)) callbacks.forEach(cb -> cb.accept(this));
            } catch (CommandExecuteException e) {
                // todo: this
            } catch (NullPointerException e) {
                // todo: this
            }
        }
    }

    @Override
    public String toString() {
        return getName() + " - " + getDescription();
    }
}
