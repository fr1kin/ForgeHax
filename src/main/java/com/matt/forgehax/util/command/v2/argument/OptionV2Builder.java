package com.matt.forgehax.util.command.v2.argument;

import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created on 1/30/2018 by fr1kin
 */
public class OptionV2Builder<E> {
    private Collection<String> names = Lists.newArrayList();
    private String description = null;
    private boolean required = false;
    private ArgumentV2<E> argument = null;
    private InputInterpreter.Function<OptionV2<E>> predictor = null;

    /**
     * Name(s) to be associated with this option
     * @param names name(s)
     * @return this
     */
    public OptionV2Builder<E> names(Collection<String> names) {
        this.names.addAll(names);
        return this;
    }
    public OptionV2Builder<E> names(String... names) {
        return this.names(Arrays.asList(names));
    }

    /**
     * Description for this option
     * @param description describe what this option does
     * @return this
     */
    public OptionV2Builder<E> description(String description) {
        this.description = description;
        return this;
    }

    public OptionV2Builder<E> required(boolean required) {
        this.required = required;
        return this;
    }

    public OptionV2Builder<E> argument(ArgumentV2<E> argument) {
        this.argument = argument;
        return this;
    }

    /**
     * Generates list of possible options given a certain input
     * @param predictor function
     * @return this
     */
    public OptionV2Builder<E> interpreter(InputInterpreter.Function<OptionV2<E>> predictor) {
        this.predictor = predictor;
        return this;
    }

    /**
     * Create new instance of Option.
     * Will automatically decide which child class to create it from.
     * @return new instance containing data provided
     */
    public OptionV2<E> build() {
        return asGeneric();
    }

    public OptionV2<E> asGeneric() {
        return OptionV2Generic.Factory.make(names, description, required, argument, predictor);
    }
}
