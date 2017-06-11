package com.matt.forgehax.util.mod;

import com.matt.forgehax.util.command.Command;
import com.matt.forgehax.util.command.CommandBuilder;

/**
 * Created on 6/1/2017 by fr1kin
 */
@Deprecated
public abstract class CommandMod extends SilentMod {
    private Command command = null;

    public CommandMod(String name, String desc) {
        super(name, desc);
    }

    @Override
    protected void onUnload() {
        if(command != null) command.leaveParent();
    }

    public abstract Command generate(CommandBuilder commandBuilder);
}
