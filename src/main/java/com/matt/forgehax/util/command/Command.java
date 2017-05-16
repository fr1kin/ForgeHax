package com.matt.forgehax.util.command;

import com.google.common.collect.Sets;
import com.matt.forgehax.Wrapper;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
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

    public void run(String[] args) throws CommandExecuteException, NullPointerException {
        if(processor != null) {
            Objects.requireNonNull(args, "args[] is null");
            OptionSet options = parser.parse(args);
            if(autoHelpText && options.has("help"))
                Wrapper.printMessageNaked(getOptionHelpText());
            else if(processor.apply(options))
                callbacks.forEach(cb -> cb.accept(this));
        }
    }

    @Override
    public String toString() {
        return getName() + " - " + getDescription();
    }
}
