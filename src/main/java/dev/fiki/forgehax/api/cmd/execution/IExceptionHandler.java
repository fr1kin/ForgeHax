package dev.fiki.forgehax.api.cmd.execution;

public interface IExceptionHandler {
  void onThrown(Throwable throwable, IConsole output);
}
