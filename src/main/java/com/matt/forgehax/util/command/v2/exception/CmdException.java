package com.matt.forgehax.util.command.v2.exception;

/**
 * Created on 1/30/2018 by fr1kin
 */
public class CmdException extends Exception {
    public CmdException(String msg) {
        super(msg);
    }

    public static class CreationFailure extends CmdException {
        public CreationFailure(String msg) {
            super(msg);
        }
    }

    public static class Unknown extends CmdException {
        public Unknown(String msg) {
            super(msg);
        }
    }

    public static class Ambiguous extends CmdException {
        public Ambiguous(String msg) {
            super(msg);
        }
    }
}
