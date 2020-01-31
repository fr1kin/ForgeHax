package dev.fiki.forgehax.main.util.command.callbacks;

import dev.fiki.forgehax.main.util.command.Command;
import dev.fiki.forgehax.main.util.console.ConsoleWriter;

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
