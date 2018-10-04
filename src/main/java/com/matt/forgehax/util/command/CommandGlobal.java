package com.matt.forgehax.util.command;

import com.matt.forgehax.util.command.exception.CommandExecuteException;
import javax.annotation.Nonnull;
import joptsimple.internal.Strings;

/** Created on 6/2/2017 by fr1kin */
public class CommandGlobal extends CommandStub {
  private static final CommandGlobal INSTANCE = new CommandGlobal();

  public static CommandGlobal getInstance() {
    return INSTANCE;
  }

  private CommandGlobal() {
    super(
        CommandBuilders.getInstance()
            .newStubBuilder()
            .name(Strings.EMPTY)
            .helpOption(false)
            .getData());
  }

  @Override
  public boolean isGlobal() {
    return true;
  }

  @Override
  public String getName() {
    return Strings.EMPTY;
  }

  @Override
  public String getAbsoluteName() {
    return Strings.EMPTY;
  }

  @Override
  public void run(@Nonnull String[] args) throws CommandExecuteException, NullPointerException {
    if (!processChildren(args)) {
      if (args.length > 0)
        throw new CommandExecuteException(String.format("Unknown command \"%s\"", args[0]));
      else throw new CommandExecuteException("Missing argument(s)");
    }
  }
}
