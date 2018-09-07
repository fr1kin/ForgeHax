package com.matt.forgehax.util.command.v2.exception;

import com.matt.forgehax.util.command.v2.ICmd;

public class CmdUnknownException extends BaseCmdException {
    private final String input;

    public CmdUnknownException(ICmd command, String input) {
        super(command, "unknown command");
        this.input = input;
    }

    public String getInput() {
        return input;
    }
}
