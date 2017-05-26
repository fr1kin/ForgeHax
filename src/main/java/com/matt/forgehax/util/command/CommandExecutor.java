package com.matt.forgehax.util.command;

import com.matt.forgehax.Wrapper;

/**
 * Created on 5/15/2017 by fr1kin
 */
public class CommandExecutor {
    public static void run(String code) {
        try {
            String[] arguments = CommandLine.translate(code);
            if(arguments.length > 0) {
                String name = arguments[0];

                Command command = CommandRegistry.getCommand(name);
                if(command == null) throw new CommandExecuteException(String.format("'%s' is not a registered command", name));

                command.run(CommandLine.forward(arguments));
            } else throw new IllegalArgumentException("Missing arguments");
        } catch (Exception e) {
            // TODO: handle
            Wrapper.printMessage(e.getMessage());
        }
    }
}
