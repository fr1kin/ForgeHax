package com.matt.forgehax.util.command.v2;

import com.matt.forgehax.util.command.v2.argument.IArg;
import com.matt.forgehax.util.command.v2.callback.ICmdCallback;
import com.matt.forgehax.util.command.v2.exception.CmdMissingArgumentException;
import com.matt.forgehax.util.command.v2.flag.ICmdFlag;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import joptsimple.internal.Strings;
import org.apache.commons.lang3.StringUtils;

/** Created on 1/30/2018 by fr1kin */
public class CmdHelper {
  private static final Pattern VALID_CHARACTERS_PATTERN = Pattern.compile("^[A-Za-z0-9-_]+$");

  public static void requireArgument(String[] args, ICmd cmd, IArg<?> arg)
      throws CmdMissingArgumentException {
    if (args.length == 0 || Strings.isNullOrEmpty(CmdHelper.firstOf(args)))
      throw new CmdMissingArgumentException(cmd, arg);
  }

  public static boolean isNameValid(String name) {
    return VALID_CHARACTERS_PATTERN.matcher(name).matches();
  }

  public static String getBestMatchingName(ICmd command, String name, String defaultTo) {
    return getSortedMatching(command.getAllNames(), name, true)
        .stream()
        .findFirst()
        .orElse(defaultTo);
  }

  public static String getBestMatchingName(ICmd command, String name) {
    return getBestMatchingName(command, name, null);
  }

  public static List<String> getSortedMatching(
      Collection<String> strings, String match, final boolean ignoreCase) {
    Objects.requireNonNull(match);
    final String m = ignoreCase ? match.toLowerCase() : match;
    return strings
        .stream()
        .filter(str -> ignoreCase ? str.toLowerCase().startsWith(m) : str.startsWith(m))
        .sorted(
            (str1, str2) -> {
              String s1 = ignoreCase ? str1.toLowerCase() : str1;
              String s2 = ignoreCase ? str2.toLowerCase() : str2;
              int d1 = StringUtils.getLevenshteinDistance(s1, m);
              int d2 = StringUtils.getLevenshteinDistance(s2, m);
              int diff = d1 - d2;
              return diff == 0 ? s1.compareTo(s2) : diff;
            })
        .collect(Collectors.toList());
  }

  public static <T extends ICmd> T registerAll(
      T cmd, Iterable<ICmdCallback> callbacks, Iterable<Enum<? extends ICmdFlag>> flags) {
    callbacks.forEach(cmd::addCallback);
    flags.forEach(cmd::addFlag);
    return cmd;
  }

  public static int compare(ICmd cmd1, ICmd cmd2) {
    Objects.requireNonNull(cmd1);
    Objects.requireNonNull(cmd2);
    return Objects.compare(cmd1, cmd2, ICmd::compareTo);
  }

  public static String firstOf(String[] args, String defaultTo) {
    return args.length > 0 ? args[0] : defaultTo;
  }

  public static String firstOf(String[] args) {
    return firstOf(args, null);
  }

  public static String lastOf(String[] args, String defaultTo) {
    return args.length > 0 ? args[args.length - 1] : defaultTo;
  }

  public static String lastOf(String[] args) {
    return lastOf(args, null);
  }

  public static String[] nextOf(String[] args) {
    return args.length > 0 ? Arrays.copyOfRange(args, 1, args.length) : new String[0];
  }
}
