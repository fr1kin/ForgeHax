package dev.fiki.forgehax.main.util.cmd.exception;

public class CommandSuccessfulExecutionException extends RuntimeException {
  public CommandSuccessfulExecutionException(String message, Object... args) {
    super(String.format(message, args));
  }
}
