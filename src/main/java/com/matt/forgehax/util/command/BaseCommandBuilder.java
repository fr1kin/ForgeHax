package com.matt.forgehax.util.command;

import com.google.common.collect.*;
import com.matt.forgehax.util.command.callbacks.CallbackData;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.lwjgl.input.Keyboard;

import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created on 6/6/2017 by fr1kin
 */
public abstract class BaseCommandBuilder<T extends BaseCommandBuilder, R extends Command> {
    protected final Map<String, Object> data = Maps.newHashMap();

    private Collection<Consumer<OptionParser>> optionBuilders;
    private Collection<Consumer<ExecuteData>> processors;
    private Multimap<CallbackType, Consumer<CallbackData>> callbacks;

    @SuppressWarnings("unchecked")
    protected T insert(String entry, Object o) {
        Object g = data.get(entry);
        if(g != null && o == null)
            data.remove(entry);
        else if(o != null)
            data.put(entry, o);
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    protected <E> Collection<E> getCallbacks(CallbackType type) {
        if(callbacks == null) {
            callbacks = Multimaps.newSetMultimap(Maps.newHashMap(), Sets::newHashSet);
            data.put(Command.CALLBACKS, callbacks);
        }
        return (Collection<E>)callbacks.get(type);
    }

    protected Collection<Consumer<OptionParser>> getOptionBuilders() {
        if(optionBuilders == null) {
            optionBuilders = Lists.newArrayList();
            data.put(Command.OPTIONBUILDERS, optionBuilders);
        }
        return optionBuilders;
    }

    protected Collection<Consumer<ExecuteData>> getProcessors() {
        if(processors == null) {
            processors = Lists.newArrayList();
            data.put(Command.PROCESSORS, processors);
        }
        return processors;
    }

    protected BaseCommandBuilder() {}

    public T parent(Command parent) {
        return insert(Command.PARENT, parent);
    }

    public T name(String name) {
        return insert(Command.NAME, name);
    }

    public T description(String description) {
        return insert(Command.DESCRIPTION, description);
    }

    public T processor(Consumer<ExecuteData> processor) {
        getProcessors().add(processor);
        return (T)this;
    }

    public T options(Consumer<OptionParser> optionBuilder) {
        getOptionBuilders().add(optionBuilder);
        return (T)this;
    }

    public T help(Consumer<OptionSet> consumer) {
        return insert(Command.HELP, consumer);
    }

    public T success(Consumer<CallbackData> consumer) {
        getCallbacks(CallbackType.SUCCESS).add(consumer);
        return (T)this;
    }

    public T failure(Consumer<CallbackData> consumer) {
        getCallbacks(CallbackType.FAILURE).add(consumer);
        return (T)this;
    }

    public T kpressed(Consumer<CallbackData> consumer) {
        getCallbacks(CallbackType.KEY_PRESSED).add(consumer);
        return (T)this;
    }

    public T kdown(Consumer<CallbackData> consumer) {
        getCallbacks(CallbackType.KEY_DOWN).add(consumer);
        return (T)this;
    }

    public T bind(int keyCode) {
        return insert(Command.KEYBIND, keyCode);
    }

    public T bind() {
        return bind(Keyboard.KEY_NONE);
    }

    public T nobind() {
        return bind(-1);
    }

    public T helpOption(boolean b) {
        return insert(Command.HELPAUTOGEN, b);
    }

    public T bindOptions(boolean b) {
        return insert(Command.KEYBIND_OPTIONS, b);
    }

    public Map<String, Object> getData() {
        return data;
    }

    public abstract R build();
}
