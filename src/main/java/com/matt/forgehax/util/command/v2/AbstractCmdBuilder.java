package com.matt.forgehax.util.command.v2;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.matt.forgehax.util.command.v2.argument.*;
import com.matt.forgehax.util.command.v2.callback.ICmdCallback;
import com.matt.forgehax.util.command.v2.flag.ICmdFlag;
import java.util.*;
import java.util.function.Function;
import javax.annotation.Nullable;

/** Created on 12/26/2017 by fr1kin */
public abstract class AbstractCmdBuilder<R extends AbstractCmdBuilder, V extends ICmd> {
  @Nullable protected final IParentCmd parent;

  protected String name;
  protected String description;

  protected List<String> aliases = Lists.newArrayList();

  protected List<IArg<?>> arguments = Lists.newArrayList();
  protected List<IOption<?>> options = Lists.newArrayList();

  protected Set<Enum<? extends ICmdFlag>> flags = Sets.newHashSet();
  protected Set<ICmdCallback> callbacks = Sets.newHashSet();

  public AbstractCmdBuilder(@Nullable IParentCmd parent) {
    this.parent = parent;
  }

  protected R getThis() {
    return (R) this;
  }

  /**
   * Set the name of the command.
   *
   * @param name commands name
   * @return this
   */
  public R name(String name) {
    this.name = name;
    return getThis();
  }

  /**
   * Set the description for the command
   *
   * @param description describes what the command does
   * @return this
   */
  public R description(String description) {
    this.description = description;
    return getThis();
  }

  /**
   * Sets aliases for the command that can be alternatively used
   *
   * @param aliases alternative names for this command
   * @return this
   */
  public R aliases(Collection<String> aliases) {
    this.aliases.addAll(aliases);
    return getThis();
  }

  public R aliases(String... aliases) {
    return aliases(Arrays.asList(aliases));
  }

  /**
   * Adds argument by providing a builder, which returns the argument object to use
   *
   * @param function provides an argument builder, returns an argument
   * @param <T> type
   * @return this
   */
  public <T> R argument(Function<ArgBuilder<T>, IArg<T>> function) {
    this.arguments.add(function.apply(new ArgBuilder<T>()));
    return getThis();
  }

  /**
   * Adds arguments with varargs
   *
   * @param arguments argument(s) to add
   * @return this
   */
  public R arguments(IArg<?>... arguments) {
    this.arguments.addAll(Arrays.asList(arguments));
    return getThis();
  }

  /**
   * Add option via function that provides a builder.
   *
   * @param function provides a builder, returns the object to add
   * @return this
   */
  public R option(Function<Object, IOption> function) {
    this.options.add(function.apply(new Object()));
    return getThis();
  }

  /**
   * Add varargs of options
   *
   * @param options varargs of options
   * @return this
   */
  public R options(Collection<IOption<?>> options) {
    this.options.addAll(options);
    return getThis();
  }

  public R options(IOption<?>... options) {
    return options(Arrays.asList(options));
  }

  public R flags(Collection<Enum<? extends ICmdFlag>> flags) {
    this.flags.addAll(flags);
    return getThis();
  }

  public R flags(Enum<? extends ICmdFlag>[] flags) {
    return flags(Arrays.asList(flags));
  }

  public R flag(Enum<? extends ICmdFlag> flag) {
    return flags(Collections.singleton(flag));
  }

  public R callbacks(Collection<ICmdCallback> flags) {
    this.callbacks.addAll(flags);
    return getThis();
  }

  public R callbacks(ICmdCallback[] flags) {
    return callbacks(Arrays.asList(flags));
  }

  public R callback(ICmdCallback flag) {
    return callbacks(Collections.singleton(flag));
  }

  public abstract V build();
}
