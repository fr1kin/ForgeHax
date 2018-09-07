package com.matt.forgehax.util.command.v2.exception;

import com.matt.forgehax.util.command.v2.ICmd;
import com.matt.forgehax.util.command.v2.argument.IArg;

public class CmdMissingArgumentException extends BaseCmdException {
    private final IArg<?> argument;

    public CmdMissingArgumentException(ICmd command, IArg<?> argument) {
        super(command, "missing argument");
        this.argument = argument;
    }

    public IArg<?> getExpectingArgument() {
        return argument;
    }
}
