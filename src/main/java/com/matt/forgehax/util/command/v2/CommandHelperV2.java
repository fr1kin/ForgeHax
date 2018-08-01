package com.matt.forgehax.util.command.v2;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created on 1/30/2018 by fr1kin
 */
public class CommandHelperV2 {
    private static final Pattern VALID_CHARACTERS_PATTERN = Pattern.compile("^[A-Za-z0-9-_]+$");

    public static boolean isNameValid(String name) {
        return VALID_CHARACTERS_PATTERN.matcher(name).matches();
    }

    public static String getBestMatchingName(ICommandV2 command, String name, String defaultTo) {
        return getSortedMatching(command.getAllNames(), name, true).stream()
                .findFirst()
                .orElse(defaultTo);
    }
    public static String getBestMatchingName(ICommandV2 command, String name) {
        return getBestMatchingName(command, name, null);
    }

    public static List<String> getSortedMatching(Collection<String> strings, String match, final boolean ignoreCase) {
        Objects.requireNonNull(match);
        final String m = ignoreCase ? match.toLowerCase() : match;
        return strings.stream()
                .filter(str -> ignoreCase ? str.toLowerCase().startsWith(m) : str.startsWith(m))
                .sorted((str1, str2) -> {
                    String s1 = ignoreCase ? str1.toLowerCase() : str1;
                    String s2 = ignoreCase ? str2.toLowerCase() : str2;
                    int d1 = StringUtils.getLevenshteinDistance(s1, m);
                    int d2 = StringUtils.getLevenshteinDistance(s2, m);
                    int diff = d1 - d2;
                    return diff == 0 ? s1.compareTo(s2) : diff;
                })
                .collect(Collectors.toList());
    }
}
