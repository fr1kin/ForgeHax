package dev.fiki.forgehax.api.cmd.execution;

import dev.fiki.forgehax.api.cmd.ICommand;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;

import static dev.fiki.forgehax.api.cmd.CommandHelper.getExecutor;
import static dev.fiki.forgehax.api.cmd.CommandHelper.parseLine;
import static dev.fiki.forgehax.main.Common.getRootCommand;

@Builder
@AllArgsConstructor
public class CommandExecutor {
  private IExceptionHandler exceptionHandler;
  private IConsole console;

  public void run(@NonNull ICommand command, String[] args) {
    getExecutor(command).execute(() -> {
      try {
        ArgumentList list = ArgumentList.createList(command, args, console);

        ICommand nextCommand = command.onExecute(list);

        if(nextCommand != null) {
          run(nextCommand, list.getUnusedArguments());
        }
      } catch (Throwable t) {
        exceptionHandler.onThrown(t, console);
      }
    });
  }

  public void run(String[] args) {
    run(getRootCommand(), args);
  }

  public void runLine(String line) {
    run(parseLine(line));
  }
}
