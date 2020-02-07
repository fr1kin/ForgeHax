package dev.fiki.forgehax.main.util.cmd.listener;

import dev.fiki.forgehax.main.util.cmd.ICommand;

public interface IUpdateConfiguration extends ICommandListener {
  void onUpdate(ICommand command);
}
