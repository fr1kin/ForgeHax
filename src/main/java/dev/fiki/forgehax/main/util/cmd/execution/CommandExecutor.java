package dev.fiki.forgehax.main.util.cmd.execution;

import dev.fiki.forgehax.main.util.cmd.ICommand;
import dev.fiki.forgehax.main.util.mod.AbstractMod;
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
        // FIXME: Hacky fix for "${command} toggle". This should be removed when
        //        the command system is rewritten. - Kakol
        if(command.getName() == "toggle" && command.getParent() instanceof AbstractMod) {
          // Dumb shit to get java to treat parent properly
          AbstractMod parent = (AbstractMod) command.getParent();
          String[] nargs = {"enabled", parent.isEnabled() ? "0" : "1"};
          run(parent, nargs);
        } else {
         ArgumentList list = ArgumentList.createList(command, args, console);

          ICommand nextCommand = command.onExecute(list);

          if(nextCommand != null) {
            run(nextCommand, list.getUnusedArguments());
          }
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
