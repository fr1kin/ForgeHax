package dev.fiki.forgehax.main.util.cmd.execution;

import dev.fiki.forgehax.main.util.cmd.ICommand;
import dev.fiki.forgehax.main.util.cmd.flag.EnumFlag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.concurrent.Executor;

import static dev.fiki.forgehax.main.Common.*;

@Builder
@AllArgsConstructor
public class CommandExecutor {
  private IExceptionHandler exceptionHandler;
  private IConsole console;

  public void run(@NonNull ICommand command, String[] args) {
    Executor executor;

    if(command.containsFlag(EnumFlag.EXECUTOR_MAIN_THREAD)) {
      // execute on main minecraft thread
      executor = getMainThreadExecutor();
    } else if(command.containsFlag(EnumFlag.EXECUTOR_ASYNC)) {
      // execute on an async thread
      executor = getAsyncThreadExecutor();
    } else {
      // don't care, execute on current thread
      executor = new SameThreadExecutor();
    }

    executor.execute(() -> {
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

  // [code borrowed from ant.jar] Crack a command line.
  public static String[] parseLine(String toProcess) {
    if (toProcess == null || toProcess.length() == 0) {
      // no command? no string
      return new String[0];
    }
    // parse with a simple finite state machine

    final int normal = 0;
    final int inQuote = 1;
    final int inDoubleQuote = 2;
    int state = normal;
    final StringTokenizer tok = new StringTokenizer(toProcess, "\"\' ", true);
    final ArrayList<String> result = new ArrayList<String>();
    final StringBuilder current = new StringBuilder();
    boolean lastTokenHasBeenQuoted = false;

    while (tok.hasMoreTokens()) {
      String nextTok = tok.nextToken();
      switch (state) {
        case inQuote:
          if ("\'".equals(nextTok)) {
            lastTokenHasBeenQuoted = true;
            state = normal;
          } else {
            current.append(nextTok);
          }
          break;
        case inDoubleQuote:
          if ("\"".equals(nextTok)) {
            lastTokenHasBeenQuoted = true;
            state = normal;
          } else {
            current.append(nextTok);
          }
          break;
        default:
          if ("\'".equals(nextTok)) {
            state = inQuote;
          } else if ("\"".equals(nextTok)) {
            state = inDoubleQuote;
          } else if (" ".equals(nextTok)) {
            if (lastTokenHasBeenQuoted || current.length() != 0) {
              result.add(current.toString());
              current.setLength(0);
            }
          } else {
            current.append(nextTok);
          }
          lastTokenHasBeenQuoted = false;
          break;
      }
    }
    if (lastTokenHasBeenQuoted || current.length() != 0) {
      result.add(current.toString());
    }
    if (state == inQuote || state == inDoubleQuote) {
      throw new RuntimeException("unbalanced quotes in " + toProcess);
    }
    return result.toArray(new String[result.size()]);
  }

  private static class SameThreadExecutor implements Executor {
    @Override
    public void execute(Runnable runnable) {
      runnable.run();
    }
  }
}
