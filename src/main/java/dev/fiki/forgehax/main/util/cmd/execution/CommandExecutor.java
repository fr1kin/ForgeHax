package dev.fiki.forgehax.main.util.cmd.execution;

import dev.fiki.forgehax.main.util.cmd.ICommand;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;


import static dev.fiki.forgehax.main.Common.*;
import static dev.fiki.forgehax.main.util.cmd.CommandHelper.getExecutor;
import static dev.fiki.forgehax.main.util.cmd.CommandHelper.parseLine;

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
