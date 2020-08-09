package dev.fiki.forgehax.main.util.mod;

import dev.fiki.forgehax.main.util.cmd.ICommand;

import static dev.fiki.forgehax.main.Common.getRootCommand;

/**
 * Created on 6/1/2017 by fr1kin
 */
public class CommandMod extends AbstractMod {

  public CommandMod() {
    super(null);
  }

  @Override
  public boolean isHidden() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return false;
  }

  @Override
  protected void onLoad() { }

  @Override
  protected void onUnload() { }

  @Override
  protected final void onEnabled() { }

  @Override
  protected final void onDisabled() { }

  @Override
  public boolean addChild(ICommand command) {
    return getRootCommand().addChild(command);
  }
}
