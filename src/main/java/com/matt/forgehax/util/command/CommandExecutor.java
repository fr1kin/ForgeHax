package com.matt.forgehax.util.command;

import com.google.common.base.Objects;
import com.matt.forgehax.Wrapper;
import joptsimple.internal.Strings;

import java.util.Arrays;

/**
 * Created on 5/15/2017 by fr1kin
 */
public class CommandExecutor {
    public static void run(String code) {
        try {
            String[] sections = CommandLine.translate(code);
            String id = sections.length > 0 ? sections[0] : ""; // get mod (or global command)
            String mod = sections.length > 1 ? sections[1] : ""; // get property (or value if global)

            boolean isGlobal = false;
            Command command = CommandRegistry.getModRegistry(id).get(mod);
            if(command == null) {
                command = CommandRegistry.getGlobalCommand(id);
                isGlobal = true;
            }
            // if still null then this command doesn't exist
            if(command == null)
                throw new CommandExecuteException(String.format("'%s' is not a registered command", Strings.isNullOrEmpty(mod) ? id : id + " " + mod));

            // command input looks like this: <mod> <command> or just <command> if global
            // if global then arguments start at 1, otherwise it starts at 2
            command.run(sections.length > 1 ? Arrays.copyOfRange(sections, isGlobal ? 1 : 2, sections.length) : new String[] {""});
        } catch (Exception e) {
            // TODO: handle
            Wrapper.printMessage(e.getMessage());
        }
    }
}
