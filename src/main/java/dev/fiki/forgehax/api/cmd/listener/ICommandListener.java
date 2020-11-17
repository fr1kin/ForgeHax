package dev.fiki.forgehax.api.cmd.listener;

public interface ICommandListener {
  Class<? extends ICommandListener> getListenerClassType();
}
