package com.matt.forgehax.util.command.v2;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.matt.forgehax.util.command.v2.argument.ArgumentHelper;
import com.matt.forgehax.util.command.v2.argument.ArgumentV2;
import com.matt.forgehax.util.command.v2.argument.OptionV2;
import com.matt.forgehax.util.command.v2.callback.ICommandCallbackV2;
import com.matt.forgehax.util.command.v2.exception.CommandExceptions;
import com.matt.forgehax.util.command.v2.exception.CommandRuntimeExceptionV2;
import com.matt.forgehax.util.command.v2.flag.ICommandFlagV2;
import com.matt.forgehax.util.typeconverter.TypeConverter;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSpecBuilder;
import joptsimple.ValueConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.matt.forgehax.util.command.v2.argument.OptionV2.Type.OPTIONAL_ARGUMENT;
import static com.matt.forgehax.util.command.v2.argument.OptionV2.Type.REQUIRED_ARGUMENT;

/**
 * Created on 12/26/2017 by fr1kin
 */
public abstract class AbstractCommandV2 implements ICommandV2 {
    private final String name;
    private final Collection<String> aliases;
    private final String description;
    private final IParentCommandV2 parent;
    private final Collection<ArgumentV2<?>> arguments;
    private final Collection<OptionV2> options;

    private final OptionParser parser;

    private final Set<ICommandFlagV2> flags = Collections.synchronizedSet(Sets.newHashSet());
    private final Set<ICommandCallbackV2> callbacks = Collections.synchronizedSet(Sets.newHashSet());

    public AbstractCommandV2(String name,
                             @Nullable Collection<String> aliases,
                             String description,
                             @Nullable IParentCommandV2 parent,
                             @Nullable Collection<ArgumentV2<?>> arguments,
                             @Nullable Collection<OptionV2> options) throws CommandRuntimeExceptionV2.CreationFailure {
        CommandExceptions.checkIfNullOrEmpty(name, "name missing");
        CommandExceptions.checkIfNullOrEmpty(description, "description missing");

        this.name = name;
        this.aliases = efficientImmutableCopy(aliases);
        this.description = description;
        this.parent = parent;
        this.arguments = efficientImmutableCopy(arguments);

        if(ArgumentHelper.isInvalidArgumentOrder(this.arguments)) throw new CommandRuntimeExceptionV2.CreationFailure("required arguments are not allowed after non-required arguments");

        this.parser = new OptionParser();

        // add all options to parser
        List<OptionV2> wrapped = Lists.newArrayList();

        if(options != null) {
            // add all options to parser
            for (OptionV2 option : options) {
                OptionSpecBuilder builder = this.parser.acceptsAll(option.getNames(), option.getDescription());

                if (option.getType() == OPTIONAL_ARGUMENT || option.getType() == REQUIRED_ARGUMENT) {
                    ArgumentV2<?> arg = option.getArgument();

                    ArgumentAcceptingOptionSpec aopt = (option.getType() == OPTIONAL_ARGUMENT ? builder.withOptionalArg() : builder.withRequiredArg()) // set required
                            .describedAs(arg.getDescription()); // set description

                    try {
                        aopt.ofType(arg.getConverter().type()); // set type
                    } catch (Throwable t) {
                        // something went wrong, we have to make our own converter
                        final TypeConverter typeConverter = arg.getConverter();
                        aopt.withValuesConvertedBy(new ValueConverter() {
                            @Override
                            public Object convert(String value) {
                                return typeConverter.parse(value);
                            }

                            @Override
                            public Class valueType() {
                                return typeConverter.type();
                            }

                            @Override
                            public String valuePattern() {
                                return null;
                            }
                        });
                    }

                    if (arg.getDefaultValue() != null) aopt.defaultsTo(arg.getDefaultValue()); // set default value
                }
                wrapped.add(option.copy().fromDescriptor(builder).asJOptWrapper());
            }
        }

        // copy wrapped options to immutable list
        this.options = efficientImmutableCopy(wrapped);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAbsoluteName() {
        return parent == null ? name : parent.getAbsoluteName() + "." + name;
    }

    @Nonnull
    @Override
    public Collection<String> getAliases() {
        return aliases;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Nullable
    @Override
    public IParentCommandV2 getParent() {
        return parent;
    }

    @Override
    public Collection<ArgumentV2<?>> getArguments() {
        return arguments;
    }

    @Override
    public Collection<OptionV2> getOptions() {
        return options;
    }

    @Override
    public String getHelpText(IHelpTextFormatter formatter) {
        return null; //TODO: finish
    }

    @Override
    public String getSyntaxHelpText() {
        return null; //TODO: finish
    }

    @Override
    public Collection<ICommandFlagV2> getFlags() {
        synchronized (flags) {
            return Lists.newArrayList(flags);
        }
    }

    protected Collection<ICommandFlagV2> _getFlags() {
        return flags;
    }

    @Override
    public boolean addFlag(ICommandFlagV2 flag) {
        synchronized (flags) {
            return flags.add(flag);
        }
    }

    @Override
    public boolean removeFlag(ICommandFlagV2 flag) {
        synchronized (flags) {
            return flags.remove(flag);
        }
    }

    @Override
    public boolean containsFlag(ICommandFlagV2 flag) {
        synchronized (flags) {
            return flags.contains(flag);
        }
    }

    @Nonnull
    @Override
    public Collection<ICommandCallbackV2> getCallbacks() {
        synchronized (callbacks) {
            return Sets.newHashSet(callbacks);
        }
    }

    @Nonnull
    @Override
    public <T extends ICommandCallbackV2> Collection<T> getCallbacksOfType(Class<T> clazz) {
        synchronized (callbacks) {
            return callbacks.stream()
                    .filter(clazz::isInstance)
                    .map(clazz::cast)
                    .collect(Collectors.toSet());
        }
    }

    protected Collection<ICommandCallbackV2> _getCallbacks() {
        return callbacks;
    }

    @Override
    public boolean addCallback(ICommandCallbackV2 callback) {
        synchronized (callbacks) {
            return callbacks.add(callback);
        }
    }

    @Override
    public boolean removeCallback(ICommandCallbackV2 callback) {
        synchronized (callbacks) {
            return callbacks.remove(callback);
        }
    }

    protected OptionParser _getParser() {
        return parser;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ICommandV2
                && getAbsoluteName().equalsIgnoreCase(((ICommandV2) obj).getAbsoluteName());
    }

    //
    // util
    //

    protected static <T> List<T> efficientImmutableCopy(@Nullable Collection<T> collection) {
        if(collection == null || collection.isEmpty())
            return Collections.emptyList();
        else if(collection.size() == 1)
            return Collections.singletonList(collection.iterator().next());
        else
            return ImmutableList.copyOf(collection);
    }
}
