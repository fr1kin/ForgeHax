package com.matt.forgehax.util.command;

import com.google.common.collect.*;
import com.matt.forgehax.Globals;
import com.matt.forgehax.Wrapper;
import com.matt.forgehax.mods.BaseMod;

import java.util.*;

/**
 * Created on 5/14/2017 by fr1kin
 */
public class CommandRegistry implements Globals {
    private static final Map<String, Command> GLOBAL_ID_TO_COMMAND_REGISTRY = Maps.newTreeMap(String.CASE_INSENSITIVE_ORDER);

    public static void register(Command command) {
        GLOBAL_ID_TO_COMMAND_REGISTRY.put(CommandLine.makeParserFriendly(command.getName()), command);
    }

    public static void registerAll(Collection<Command> commands) {
        commands.forEach(CommandRegistry::register);
    }

    public static void registerAll(Command... commands) {
        registerAll(Arrays.asList(commands));
    }

    public static void unregister(Command command) {
        GLOBAL_ID_TO_COMMAND_REGISTRY.remove(CommandLine.makeParserFriendly(command.getName()));
    }

    public static void unregisterAll(Collection<Command> commands) {
        commands.forEach(CommandRegistry::unregister);
    }

    public static void unregisterAll(Command... commands) {
        unregisterAll(Arrays.asList(commands));
    }

    public static Command getCommand(String id) {
        return GLOBAL_ID_TO_COMMAND_REGISTRY.get(id);
    }

    public static Map<String, Command> getCommands() {
        return Collections.unmodifiableMap(GLOBAL_ID_TO_COMMAND_REGISTRY);
    }
}
