package dev.fiki.forgehax.api.cmd.execution;

public interface IConsole {
  void inform(String message, Object... args);
  void warn(String message, Object... args);
  void error(String message, Object... args);
}
