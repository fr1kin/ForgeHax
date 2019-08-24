package com.matt.forgehax.util.command;

/**
 * Created on 5/14/2017 by fr1kin
 */
public class CommandBuilder extends BaseCommandBuilder<CommandBuilder, Command> {
  
  @Override
  public Command build() {
    return new Command(data);
  }
}
