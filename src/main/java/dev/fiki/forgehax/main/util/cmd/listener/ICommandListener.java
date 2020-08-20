package dev.fiki.forgehax.main.util.cmd.listener;

public interface ICommandListener {
  Class<? extends ICommandListener> getListenerClassType();
}
