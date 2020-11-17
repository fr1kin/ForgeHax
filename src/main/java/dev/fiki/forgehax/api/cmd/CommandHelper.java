package dev.fiki.forgehax.api.cmd;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import dev.fiki.forgehax.api.cmd.flag.EnumFlag;
import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.StringTokenizer;
import java.util.concurrent.Executor;

import static dev.fiki.forgehax.main.Common.getAsyncThreadExecutor;
import static dev.fiki.forgehax.main.Common.getMainThreadExecutor;

public class CommandHelper {
  private static final Comparator<ICommand> COMMAND_COMPARATOR =
      Comparator.comparing(ICommand::getName, String.CASE_INSENSITIVE_ORDER);

  public static boolean isHiddenFlag(ICommand command) {
    return command.containsFlag(EnumFlag.HIDDEN);
  }

  public static boolean isVisibleFlag(ICommand command) {
    return !isHiddenFlag(command);
  }

  public static Comparator<ICommand> commandComparator() {
    return COMMAND_COMPARATOR;
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

  public static Executor getExecutor(ICommand command) {
    if (command.containsFlag(EnumFlag.EXECUTOR_MAIN_THREAD)) {
      // execute on main minecraft thread
      return getMainThreadExecutor();
    } else if (command.containsFlag(EnumFlag.EXECUTOR_ASYNC)) {
      // execute on an async thread
      return getAsyncThreadExecutor();
    } else {
      // don't care, execute on current thread
      return new SameThreadExecutor();
    }
  }

  public static Executor getWriteExecutor(ICommand command) {
    if (command.containsFlag(EnumFlag.SERIALIZE_ASYNC)) {
      return getAsyncThreadExecutor();
    } else {
      return new SameThreadExecutor();
    }
  }

  @SneakyThrows
  public static void writeConfigFile(JsonElement root, Path file) {
    Files.write(file,
        createGson().toJson(root).getBytes(StandardCharsets.UTF_8),
        StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
  }

  @SneakyThrows
  public static JsonElement readConfigFile(Path file) {
    return new JsonParser().parse(new String(Files.readAllBytes(file), StandardCharsets.UTF_8));
  }

  public static Gson createGson() {
    return new GsonBuilder().setPrettyPrinting().create();
  }

  private static class SameThreadExecutor implements Executor {
    @Override
    public void execute(Runnable runnable) {
      runnable.run();
    }
  }
}
