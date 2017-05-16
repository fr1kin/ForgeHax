package com.matt.forgehax.util.command;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.minecraftforge.common.config.Property;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created on 5/14/2017 by fr1kin
 */
public class CommandWithProperty extends Command {
    private final Property property;

    public CommandWithProperty(Property property, Consumer<OptionParser> buildParser, Function<OptionSet, Boolean> process, Collection<Consumer<Command>> callbacks, boolean b) {
        super(property.getName(), property.getComment(), buildParser, process, callbacks, b);
        this.property = property;
    }

    public Property getProperty() {
        return property;
    }
}
