package com.matt.forgehax.util.command;

import com.matt.forgehax.util.command.exception.CommandBuildException;
import com.matt.forgehax.util.command.exception.CommandExecuteException;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Created on 6/8/2017 by fr1kin
 */
public class CommandStub extends Command {
    protected CommandStub(Map<String, Object> data) throws CommandBuildException {
        super(data);
    }

    @Override
    public void run(@Nonnull String[] args) throws CommandExecuteException, NullPointerException {
        if(!processChildren(args)) {
            if(args.length > 0)
                throw new CommandExecuteException(String.format("Unknown command \"%s\"", args[0]));
            else
                throw new CommandExecuteException("Missing argument(s)");
        }
    }
}
