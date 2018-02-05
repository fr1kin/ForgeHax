package com.matt.forgehax.util.command.v2;

import com.google.common.collect.Lists;
import com.matt.forgehax.util.command.v2.argument.*;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Created on 12/26/2017 by fr1kin
 */
public class CommandBuilderV2 {
    @Nullable private final IParentCommandV2 parent;

    private String name;
    private String description;

    private List<String> aliases = Lists.newArrayList();

    private List<ArgumentV2<?>> arguments = Lists.newArrayList();
    private List<OptionV2> options = Lists.newArrayList();

    public CommandBuilderV2(@Nullable IParentCommandV2 parent) {
        this.parent = parent;
    }
    public CommandBuilderV2() {
        this(null);
    }

    /**
     * Set the name of the command.
     * @param name commands name
     * @return this
     */
    public CommandBuilderV2 name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Set the description for the command
     * @param description describes what the command does
     * @return this
     */
    public CommandBuilderV2 description(String description) {
        this.description = description;
        return this;
    }

    /**
     * Sets aliases for the command that can be alternatively used
     * @param aliases alternative names for this command
     * @return this
     */
    public CommandBuilderV2 aliases(Collection<String> aliases) {
        this.aliases.addAll(aliases);
        return this;
    }
    public CommandBuilderV2 aliases(String... aliases) {
        return aliases(Arrays.asList(aliases));
    }

    /**
     * Adds argument by providing a builder, which returns the argument object to use
     * @param function provides an argument builder, returns an argument
     * @param <T> type
     * @return this
     */
    public <T> CommandBuilderV2 argument(Function<ArgumentV2Builder<T>, ArgumentV2<T>> function) {
        this.arguments.add(function.apply(new ArgumentV2Builder<T>()));
        return this;
    }

    /**
     * Adds arguments with varargs
     * @param arguments argument(s) to add
     * @return this
     */
    public CommandBuilderV2 arguments(ArgumentV2<?>... arguments) {
        this.arguments.addAll(Arrays.asList(arguments));
        return this;
    }

    /**
     * Add option via function that provides a builder.
     * @param function provides a builder, returns the object to add
     * @return this
     */
    public CommandBuilderV2 option(Function<OptionV2Builder, OptionV2> function) {
        this.options.add(function.apply(new OptionV2Builder()));
        return this;
    }

    /**
     * Add varargs of options
     * @param options varargs of options
     * @return this
     */
    public CommandBuilderV2 options(Collection<OptionV2> options) {
        this.options.addAll(options);
        return this;
    }
    public CommandBuilderV2 options(OptionV2... options) {
        return options(Arrays.asList(options));
    }

    public IParentCommandV2 asParentCommand() {
        return new ParentCommandV2(name, aliases, description, parent, options);
    }
}
