package dev.fiki.forgehax.main.util.mod;

import com.google.common.collect.Lists;
import dev.fiki.forgehax.main.util.cmd.ICommand;
import joptsimple.internal.Strings;

import static dev.fiki.forgehax.main.Common.*;

/**
 * Created on 6/1/2017 by fr1kin
 */
public class CommandMod extends ServiceMod {
  
  public CommandMod(String name, String desc) {
    super(name, desc);
  }
  
  public CommandMod(String name) {
    super(name, Strings.EMPTY);
  }

  @Override
  public boolean addChild(ICommand command) {
    return getRootCommand().addChild(command);
  }
}
