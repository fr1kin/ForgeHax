package dev.fiki.forgehax.main.util.cmd.execution;

import com.google.common.collect.Lists;
import dev.fiki.forgehax.main.util.cmd.ICommand;
import dev.fiki.forgehax.main.util.cmd.argument.IArgument;
import dev.fiki.forgehax.main.util.cmd.value.IValue;
import dev.fiki.forgehax.main.util.cmd.value.Value;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
public class ArgumentList {
  public static ArgumentList createList(ICommand command, String[] args, IConsole output) {
    // iterate over all the argument(s) and construct a value
    // from each argument with the given arguments
    int start = 0;
    int length = args.length;

    List<IValue<?>> values = Lists.newArrayList();
    for(IArgument<?> arg : command.getArguments()) {
      if(length <= 0 || length < arg.getMinArgumentsConsumed()) {
        if(arg.isOptional()) {
          values.add(new Value(arg.getDefaultValue(), arg));
          continue;
        } else {
          throw new IllegalStateException("expected "
              + arg.getMinArgumentsConsumed()
              + " arguments, got "
              + length);
        }
      }

      int range = Math.min(arg.getMaxArgumentsConsumed(), length + 1);

      // remove the arguments from this list
      length -= range;

      String[] valueFrom = Arrays.copyOfRange(args, start, range);
      values.add(new Value(arg.parse(String.join(" ", valueFrom)), arg));

      start = range;
    }

    String[] unused = start >= args.length
        ? new String[0]
        : Arrays.copyOfRange(args, start, args.length);

    return new ArgumentList(values, unused, output);
  }

  private final List<IValue<?>> arguments;

  @Getter
  @NonNull
  private final String[] unusedArguments;

  @Getter
  @NonNull
  private final IConsole console;

  @SuppressWarnings("unchecked")
  public <T> IValue<T> get(int index) {
    return (IValue<T>) arguments.get(index);
  }

  @SuppressWarnings("unchecked")
  public <T> IValue<T> get(String label) {
    Objects.requireNonNull(label, "label is null");
    for(IValue<?> val : arguments) {
      if(label.equalsIgnoreCase(val.getConverter().getLabel())) {
        return (IValue<T>) val;
      }
    }
    throw new IllegalArgumentException("unknown argument label \"" + label + "\"");
  }

  public <T> IValue<T> getFirst() {
    return get(0);
  }

  public <T> IValue<T> getSecond() {
    return get(1);
  }

  public <T> IValue<T> getThird() {
    return get(2);
  }

  public void inform(String str, Object... fmt) {
    console.inform(str, fmt);
  }

  public void warn(String str, Object... fmt) {
    console.warn(str, fmt);
  }

  public void error(String str, Object... fmt) {
    console.error(str, fmt);
  }
}
