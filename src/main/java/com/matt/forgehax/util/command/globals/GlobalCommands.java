package com.matt.forgehax.util.command.globals;

import com.matt.forgehax.util.command.CommandRegistry;

/**
 * Created on 5/27/2017 by fr1kin
 */
public class GlobalCommands {
    public static void registerAll() {
        CommandRegistry.register(BindCommand.newInstance());
        CommandRegistry.register(BlocksCommand.newInstance());
        CommandRegistry.register(HelpCommand.newInstance());
        CommandRegistry.register(UnbindCommand.newInstance());
    }
}
