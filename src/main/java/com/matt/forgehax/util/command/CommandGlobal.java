package com.matt.forgehax.util.command;

import joptsimple.internal.Strings;

/**
 * Created on 6/2/2017 by fr1kin
 */
public class CommandGlobal extends CommandStub {
    private static final CommandGlobal INSTANCE = new CommandGlobal();

    public static CommandGlobal getInstance() {
        return INSTANCE;
    }

    private CommandGlobal() {
        super(CommandBuilders.getInstance().newStubBuilder().name(Strings.EMPTY).getData());
    }

    @Override
    public boolean isGlobal() {
        return true;
    }

    @Override
    public String getName() {
        return Strings.EMPTY;
    }

    @Override
    public String getAbsoluteName() {
        return Strings.EMPTY;
    }
}
