package com.matt.forgehax.util.command.v2.exception;

import com.matt.forgehax.util.command.v2.argument.IArg;

/** Created on 1/30/2018 by fr1kin */
public class CmdRuntimeException extends RuntimeException {
  public CmdRuntimeException() {
    super();
  }

  public CmdRuntimeException(String msg) {
    super(msg);
  }

  public static class NullPointer extends CmdRuntimeException {
    public NullPointer(String msg) {
      super(msg);
    }
  }

  public static class ProcessingFailure extends CmdRuntimeException {
    public ProcessingFailure(String msg) {
      super(msg);
    }
  }

  public static class CreationFailure extends CmdRuntimeException {
    public CreationFailure(String msg) {
      super(msg);
    }
  }

  public static class Conflicting extends CmdRuntimeException {
    public Conflicting(String msg) {
      super(msg);
    }
  }

  public static class MissingArgument extends CmdRuntimeException {
    private final IArg<?> missingArgument;

    public MissingArgument(IArg<?> missingArgument) {
      super();
      this.missingArgument = missingArgument;
    }

    public IArg<?> getMissingArgument() {
      return missingArgument;
    }
  }
}
