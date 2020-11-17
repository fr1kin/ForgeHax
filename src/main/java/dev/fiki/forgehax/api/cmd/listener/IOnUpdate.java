package dev.fiki.forgehax.api.cmd.listener;

import dev.fiki.forgehax.api.cmd.ICommand;

public interface IOnUpdate extends ICommandListener {
  void onUpdate(ICommand command);

  @Override
  default Class<? extends ICommandListener> getListenerClassType() {
    return IOnUpdate.class;
  }
}
