package dev.fiki.forgehax.api.cmd;

import dev.fiki.forgehax.api.cmd.argument.IArgument;
import dev.fiki.forgehax.api.cmd.execution.ArgumentList;
import dev.fiki.forgehax.api.cmd.flag.EnumFlag;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.SneakyThrows;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@Getter
public final class SimpleCommand extends AbstractCommand {
  private final List<IArgument<?>> arguments;
  private final Function<ArgumentList, ICommand> executor;

  @Builder
  public SimpleCommand(IParentCommand parent,
      String name, @Singular Collection<String> aliases, String description,
      @Singular Collection<EnumFlag> flags, @Singular List<IArgument<?>> arguments,
      Consumer<ArgumentList> executor, Function<ArgumentList, ICommand> executorWithReturn) {
    super(parent, name, aliases, description, flags);
    this.arguments = arguments;

    if (executor == null && executorWithReturn == null) {
      throw new NullPointerException("executor or composedExecutor must be non null");
    }

    if (executorWithReturn != null) {
      this.executor = executorWithReturn;
    } else {
      this.executor = args -> {
        executor.accept(args);
        return null;
      };
    }

    onFullyConstructed();
  }

  @SneakyThrows
  @Override
  public ICommand onExecute(ArgumentList args) {
    return executor.apply(args);
  }

  public interface ICommandConsumer {
    void onExecute(ArgumentList args) throws Throwable;
  }
}
