package dev.fiki.forgehax.api.cmd.execution;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import dev.fiki.forgehax.api.cmd.ICommand;
import dev.fiki.forgehax.api.cmd.argument.IArgument;
import dev.fiki.forgehax.api.cmd.value.IValue;
import dev.fiki.forgehax.api.cmd.value.Value;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

@AllArgsConstructor
public class ArgumentList {
  public static ArgumentList createList(ICommand command, String[] args, IConsole output) {
    // iterate over all the argument(s) and construct a value
    // from each argument with the given arguments
    Queue<String> q = Queues.newArrayDeque(Arrays.asList(args));
    List<IValue<?>> values = Lists.newArrayList();
    for(IArgument<?> arg : command.getArguments()) {
      if(q.isEmpty() || q.size() < arg.getMinArgumentsConsumed()) {
        if(arg.isOptional()) {
          values.add(new Value(arg.getDefaultValue(), arg));
          continue;
        } else {
          throw new IllegalStateException("expected "
              + arg.getMinArgumentsConsumed()
              + " arguments, got "
              + q.size());
        }
      }

      // number of arguments to consume (exclusive)
      int range = Math.min(arg.getMaxArgumentsConsumed(), q.size());

      String[] valueFrom = new String[range];
      for (int i = 0; i < valueFrom.length; i++) {
        valueFrom[i] = q.poll();
      }

      values.add(new Value(arg.parse(String.join(" ", valueFrom)), arg));
    }

    return new ArgumentList(values, q.toArray(new String[0]), output);
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
