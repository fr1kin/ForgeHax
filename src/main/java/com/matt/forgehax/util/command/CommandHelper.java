package com.matt.forgehax.util.command;

import com.matt.forgehax.util.command.exception.CommandExecuteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import joptsimple.internal.Strings;

/**
 * Created on 5/15/2017 by fr1kin
 */
public class CommandHelper {

  private static final String[] EMPTY_STRING_ARRAY = new String[0];

  public static final String MOD_PROPERTY_SEPARATOR = ":";
  
  /**
   * Moves the argument array forward by one or returns an empty array if not possible
   */
  public static String[] forward(String[] args) {
    return args.length > 0 ? Arrays.copyOfRange(args, 1, args.length) : EMPTY_STRING_ARRAY;
  }

  public static String join(String[] args, String separator, int startIndex, int endIndex) {
    return Strings.join(
      Arrays.copyOfRange(args, startIndex, endIndex),
      com.google.common.base.Strings.nullToEmpty(separator));
  }

  public static String join(String[] args, String separator) {
    return join(args, separator, 0, args.length);
  }

  public static String toUniqueId(String parent, String child) {
    return makeParserFriendly(
      !Strings.isNullOrEmpty(parent) ? (parent + MOD_PROPERTY_SEPARATOR + child) : child);
  }

  public static String makeParserFriendly(String string) {
    return string.replaceAll(" ", "_");
  }

  public static void requireArguments(List<?> args, int requiredArguments)
    throws CommandExecuteException {
    if (args.size() < requiredArguments) {
      throw new CommandExecuteException("Missing argument(s)");
    }
  }

  /**
   * [code borrowed from ant.jar] Crack a command line.
   *
   * @param toProcess the command line to process.
   * @return the command line broken into strings. An empty or null toProcess parameter results in a
   * zero sized array.
   */
  public static String[] translate(String toProcess) {
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
}
