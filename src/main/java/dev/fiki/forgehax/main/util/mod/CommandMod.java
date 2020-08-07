package dev.fiki.forgehax.main.util.mod;

import dev.fiki.forgehax.main.util.cmd.ICommand;

import static dev.fiki.forgehax.main.Common.getRootCommand;

/**
 * Created on 6/1/2017 by fr1kin
 */
public class CommandMod extends ServiceMod {
  
  public CommandMod() {
    super();
  }

  @Override
  public boolean addChild(ICommand command) {
    return getRootCommand().addChild(command);
  }
}
