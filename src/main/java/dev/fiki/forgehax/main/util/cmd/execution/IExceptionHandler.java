package dev.fiki.forgehax.main.util.cmd.execution;

public interface IExceptionHandler {
  void onThrown(Throwable throwable, IConsole output);
}
