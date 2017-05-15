package com.matt.forgehax.util.command;

import com.google.common.collect.Maps;
import com.matt.forgehax.Globals;
import com.matt.forgehax.mods.BaseMod;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created on 5/14/2017 by fr1kin
 */
public class CommandManager implements Globals {
    public static void run(String code) {
        try {
            String[] sections = translateCommandline(code);
            // break apart ModName::CommandName
            String[] splitId = sections[0].split("::");
            boolean isGlobal = splitId.length <= 0;

            final Map<String, Command> map = getCommandsAsMap();
            //todo: finish
        } catch (Exception e) {
            // TODO: handle
        }
    }

    public static Map<String, Command> getCommandsAsMap() {
        final Map<String, Command> map = Maps.newHashMap();
        MOD.getMods().values().forEach(mod -> mod.getCommands().forEach(cmd -> {
            map.put(mod.getModName() + "::" + cmd.getName(), cmd);
        }));
        return Collections.unmodifiableMap(map);
    }

    /**
     * [code borrowed from ant.jar]
     * Crack a command line.
     * @param toProcess the command line to process.
     * @return the command line broken into strings.
     * An empty or null toProcess parameter results in a zero sized array.
     */
    public static String[] translateCommandline(String toProcess) {
        if (toProcess == null || toProcess.length() == 0) {
            //no command? no string
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
