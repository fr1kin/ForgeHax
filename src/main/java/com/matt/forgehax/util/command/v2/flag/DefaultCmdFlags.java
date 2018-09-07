package com.matt.forgehax.util.command.v2.flag;

/**
 * Created on 12/26/2017 by fr1kin
 */
public enum DefaultCmdFlags implements ICmdFlag {
    HIDDEN,
    ;

    static {
        CmdFlags.register(DefaultCmdFlags.class);
    }
}
