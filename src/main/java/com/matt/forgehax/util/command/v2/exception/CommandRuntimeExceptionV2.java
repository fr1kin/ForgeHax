package com.matt.forgehax.util.command.v2.exception;

/**
 * Created on 1/30/2018 by fr1kin
 */
public class CommandRuntimeExceptionV2 extends RuntimeException {
    public CommandRuntimeExceptionV2(String msg) {
        super(msg);
    }

    public static class NullPointer extends CommandRuntimeExceptionV2 {
        public NullPointer(String msg) {
            super(msg);
        }
    }

    public static class ProcessingFailure extends CommandRuntimeExceptionV2 {
        public ProcessingFailure(String msg) {
            super(msg);
        }
    }

    public static class CreationFailure extends CommandRuntimeExceptionV2 {
        public CreationFailure(String msg) {
            super(msg);
        }
    }

    public static class ConflictingCommands extends CommandRuntimeExceptionV2 {
        public ConflictingCommands(String msg) {
            super(msg);
        }
    }
}
