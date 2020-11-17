package dev.fiki.forgehax.api.cmd.exception;

public class CommandSuccessfulExecutionException extends RuntimeException {
  public CommandSuccessfulExecutionException(String message, Object... args) {
    super(String.format(message, args));
  }
}
