package com.matt.forgehax.util.command.v2.exception;

import com.matt.forgehax.util.command.v2.ICmd;

public class BaseCmdException extends RuntimeException {
  private final ICmd command;

  public BaseCmdException(ICmd command, String message) {
    super(message);
    this.command = command;
  }

  public BaseCmdException(ICmd command, Throwable wrapping) {
    super(wrapping);
    this.command = command;
  }

  public ICmd getCommand() {
    return command;
  }
}
