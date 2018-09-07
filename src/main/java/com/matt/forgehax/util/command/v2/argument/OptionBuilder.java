package com.matt.forgehax.util.command.v2.argument;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Sets;
import com.matt.forgehax.util.command.v2.converter.DefaultConverters;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

public class OptionBuilder<E> {
    public static <T> OptionMap<T> newOptionMap(IOption<T> parent) {
        return new OptionMap<>(parent);
    }

    //
    //
    //

    private Set<String> _names = Sets.newLinkedHashSet();
    private boolean _flag;
    private String _description;
    private IArg<E> _argument;

    public OptionBuilder<E> names(String... names) {
        _names.addAll(Arrays.asList(names));
        return this;
    }
    public OptionBuilder<E> name(String name) {
        return names(name);
    }

    /**
     * Description for this option. Preferably with details.
     * Not required. If not provided, it will use the short description.
     * @param description description of this option
     * @return this
     */
    public OptionBuilder<E> description(String description) {
        this._description = description;
        return this;
    }

    public OptionBuilder<E> argument(IArg<E> argument) {
        this._argument = argument;
        return this;
    }

    public IOption<E> build() {
        return new BaseOption<>(_names, _argument == null, _description, MoreObjects.firstNonNull(_argument, ArgHelper.emptyArgument()));
    }
}
