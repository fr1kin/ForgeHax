package com.matt.forgehax.util.command.callbacks;

import com.matt.forgehax.util.command.Command;
import com.matt.forgehax.util.console.ConsoleWriter;

/**
 * Created on 6/8/2017 by fr1kin
 */
public class CallbackData implements ConsoleWriter {
  
  private final Command command;
  
  public CallbackData(Command command) {
    this.command = command;
  }
  
  public <T extends Command> T command() {
    return (T) command;
  }
}
