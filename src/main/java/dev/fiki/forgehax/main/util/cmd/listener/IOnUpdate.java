package dev.fiki.forgehax.main.util.cmd.listener;

import dev.fiki.forgehax.main.util.cmd.ICommand;

public interface IOnUpdate extends ICommandListener {
  void onUpdate(ICommand command);

  @Override
  default Class<? extends ICommandListener> getListenerClassType() {
    return IOnUpdate.class;
  }
}
