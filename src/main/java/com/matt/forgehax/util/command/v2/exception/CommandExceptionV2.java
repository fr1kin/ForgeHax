package com.matt.forgehax.util.command.v2.exception;

/**
 * Created on 1/30/2018 by fr1kin
 */
public class CommandExceptionV2 extends Exception {
    public CommandExceptionV2(String msg) {
        super(msg);
    }

    public static class CreationFailure extends CommandExceptionV2 {
        public CreationFailure(String msg) {
            super(msg);
        }
    }

    public static class UnknowCommand extends CommandExceptionV2 {
        public UnknowCommand(String msg) {
            super(msg);
        }
    }

    public static class AmbiguousCommand extends CommandExceptionV2 {
        public AmbiguousCommand(String msg) {
            super(msg);
        }
    }
}
