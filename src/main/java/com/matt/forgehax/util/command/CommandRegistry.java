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
    private static final Map<BaseMod, Map<String, Command>> MOD_TO_COMMAND_REGISTRY = Maps.newConcurrentMap();

    public static Map<String, Command> getOrCreateModRegistry(BaseMod baseMod) {
        return MOD_TO_COMMAND_REGISTRY.computeIfAbsent(baseMod, m -> Maps.newTreeMap(String.CASE_INSENSITIVE_ORDER));
    }

    public static Map<String, Command> getModRegistry(BaseMod base) {
        return base != null ? Collections.unmodifiableMap(MOD_TO_COMMAND_REGISTRY.get(base)) : Collections.emptyMap();
    }

    public static Map<String, Command> getModRegistry(String modName) {
        for (Map.Entry<BaseMod, Map<String, Command>> entry : MOD_TO_COMMAND_REGISTRY.entrySet()) if(Objects.equals(modName.toLowerCase(), entry.getKey().getModName().toLowerCase()))
            return Collections.unmodifiableMap(entry.getValue());
        return Collections.emptyMap();
    }

    public static BaseMod getModByName(String modName) {
        for (BaseMod mod : MOD_TO_COMMAND_REGISTRY.keySet()) if(Objects.equals(modName.toLowerCase(), mod.getModName().toLowerCase()))
            return mod;
        return null;
    }

    public static boolean isModRegistered(BaseMod base) {
        return MOD_TO_COMMAND_REGISTRY.containsKey(base);
    }

    public static Command getModCommand(BaseMod mod, String name) {
        return getModRegistry(mod).get(name);
    }

    public static void register(BaseMod base, Command command) {
        Objects.requireNonNull(base, "BaseMod must be nonnull, or use registerGlobal if you don't want to associate the command with a mod");
        Objects.requireNonNull(isModRegistered(base), "No entry for mod '" + base.getModName() + "' found");
        MOD_TO_COMMAND_REGISTRY.get(base).put(command.getName(), command);
        Wrapper.getLog().info(String.format("Registered command '%s'", toUniqueId(base, command)));
    }

    public static void registerAll(final BaseMod base, Collection<Command> commands) {
        commands.forEach(command -> register(base, command));
    }

    public static void registerAll(final BaseMod base, Command... commands) {
        registerAll(base, Arrays.asList(commands));
    }

    public static void registerGlobal(Command command) {
        String name;
        GLOBAL_ID_TO_COMMAND_REGISTRY.put(name = toUniqueId(null, command), command);
        Wrapper.getLog().info(String.format("Registered global command '%s'", name));
    }

    public static void registerGlobalAll(Collection<Command> commands) {
        commands.forEach(CommandRegistry::registerGlobal);
    }

    public static void registerGlobalAll(Command... commands) {
        registerGlobalAll(Arrays.asList(commands));
    }

    public static void unregister(BaseMod base, Command command) {
        Objects.requireNonNull(base, "BaseMod must be nonnull, or use unregisterGlobal if you don't want to associate the command with a mod");
        Objects.requireNonNull(isModRegistered(base), "No entry for mod '" + base.getModName() + "' found");
        MOD_TO_COMMAND_REGISTRY.get(base).remove(command.getName());
        Wrapper.getLog().info(String.format("Unregistered command '%s'", toUniqueId(base, command)));
    }

    public static void unregisterAll(final BaseMod base, Collection<Command> commands) {
        commands.forEach(command -> unregister(base, command));
    }

    public static void unregisterAll(BaseMod base, Command... commands) {
        unregisterAll(base, Arrays.asList(commands));
    }

    public static void unregisterGlobal(Command command) {
        String name;
        GLOBAL_ID_TO_COMMAND_REGISTRY.remove(name = toUniqueId(null, command));
        Wrapper.getLog().info(String.format("Unregistered global command '%s'", name));
    }

    public static void unregisterGlobalAll(Collection<Command> commands) {
        commands.forEach(CommandRegistry::unregisterGlobal);
    }

    public static void unregisterGlobalAll(Command... commands) {
        unregisterGlobalAll(Arrays.asList(commands));
    }

    public static Command getGlobalCommand(String id) {
        return GLOBAL_ID_TO_COMMAND_REGISTRY.get(id);
    }

    public static Map<String, Command> getAllCommands() {
        final Map<String, Command> map = Maps.newHashMap();
        map.putAll(GLOBAL_ID_TO_COMMAND_REGISTRY);
        MOD_TO_COMMAND_REGISTRY.forEach((k, v) -> v.forEach((n, cmd) -> map.put(toUniqueId(k, cmd), cmd)));
        return Collections.unmodifiableMap(map);
    }

    public static Map<String, Command> getGlobalCommands() {
        return Collections.unmodifiableMap(GLOBAL_ID_TO_COMMAND_REGISTRY);
    }

    private static String toUniqueId(BaseMod base, Command command) {
        return CommandLine.toUniqueId(base != null ? base.getModName() : null, command.getName());
    }
}
