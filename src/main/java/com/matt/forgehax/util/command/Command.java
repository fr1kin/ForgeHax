package com.matt.forgehax.util.command;

import com.google.common.collect.Sets;
import com.matt.forgehax.Wrapper;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created on 5/14/2017 by fr1kin
 */
public class Command implements Comparable<Command> {
    private final String name;
    private final String description;

    protected final OptionParser parser = new OptionParser();
    protected final Function<OptionSet, Boolean> processor;

    protected final Set<Command> childCommands = Sets.newHashSet();
    protected final Set<Consumer<Command>> callbacks = Sets.newHashSet();

    private final boolean autoHelpText;

    public Command(String name, String description, Consumer<OptionParser> buildParser, Function<OptionSet, Boolean> processor, Collection<Consumer<Command>> callbacks, boolean autoHelpText) {
        this.name = name;
        this.description = description;
        this.processor = processor;
        this.autoHelpText = autoHelpText;
        if(callbacks != null) this.callbacks.addAll(callbacks);
        if(autoHelpText) parser.acceptsAll(Arrays.asList("help", "?"), "Help text for options");
        if(buildParser != null) buildParser.accept(parser);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getOptionHelpText() {
        StringWriter writer = new StringWriter();
        try {
            parser.printHelpOn(writer);
        } catch (IOException e) {
            ;
        }
        return writer.toString();
    }

    public Command addChildCommand(Command child) {
        return childCommands.add(child) ? child : null;
    }

    public boolean removeChildCommand(Command child) {
        return child != null && childCommands.remove(child);
    }

    public Collection<Command> getChildCommands() {
        return Collections.unmodifiableCollection(childCommands);
    }

    public Consumer<Command> addChangeCallback(Consumer<Command> consumer) {
        return callbacks.add(consumer) ? consumer : null;
    }

    public boolean removeChangeCallback(Consumer<Command> consumer) {
        return consumer != null && callbacks.remove(consumer);
    }

    protected boolean processChildCommands(String[] args) {
        if(args.length > 0) {
            String cmd = args[0];
            for (Command command : childCommands) if (command.isMatchingNameCaseInsensitive(cmd)) {
                String[] clip = Arrays.copyOfRange(args, 1, args.length);
                command.run(clip);
                return true;
            }
        }
        return false; // no child commands processed
    }

    public void run(String[] args) throws CommandExecuteException, NullPointerException {
        if(processor != null) {
            Objects.requireNonNull(args, "args[] is null");
            if(!processChildCommands(args)) { // attempt to match child commands first
                OptionSet options = parser.parse(args);
                if (autoHelpText && options.has("help"))
                    Wrapper.printMessageNaked(getOptionHelpText());
                else if (processor.apply(options))
                    callbacks.forEach(cb -> cb.accept(this));
            }
        }
    }

    public boolean isMatchingNameCaseInsensitive(String nameIn) {
        return String.CASE_INSENSITIVE_ORDER.compare(getName(), nameIn) == 0;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Command && isMatchingNameCaseInsensitive(((Command) o).getName());
    }

    @Override
    public int compareTo(Command o) {
        return String.CASE_INSENSITIVE_ORDER.compare(getName(), ((Command) o).getName());
    }

    @Override
    public String toString() {
        return getName() + " - " + getDescription();
    }
}
