package dev.fiki.forgehax.main.util.cmd;

import dev.fiki.forgehax.main.util.cmd.argument.IArgument;
import dev.fiki.forgehax.main.util.cmd.flag.EnumFlag;
import dev.fiki.forgehax.main.util.cmd.execution.ArgumentList;
import lombok.*;

import java.util.Collection;
import java.util.List;

@Getter
public final class SimpleCommand extends AbstractCommand {
  private final List<IArgument<?>> arguments;
  private final ICommandConsumer executor;

  @Builder
  public SimpleCommand(IParentCommand parent,
      String name, @Singular Collection<String> aliases, String description,
      @Singular Collection<EnumFlag> flags,
      @Singular List<IArgument<?>> arguments, @NonNull ICommandConsumer executor) {
    super(parent, name, aliases, description, flags);
    this.arguments = arguments;
    this.executor = executor;
    onFullyConstructed();
  }

  @SneakyThrows
  @Override
  public ICommand onExecute(ArgumentList args) {
    executor.onExecute(args);
    return null;
  }

  public interface ICommandConsumer {
    void onExecute(ArgumentList args) throws Throwable;
  }
}
