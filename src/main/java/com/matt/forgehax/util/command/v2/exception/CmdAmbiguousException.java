package com.matt.forgehax.util.command.v2.exception;

import com.google.common.collect.ImmutableList;
import com.matt.forgehax.util.command.v2.ICmd;
import java.util.Collection;

public class CmdAmbiguousException extends BaseCmdException {
  private final String input;
  private final Collection<? extends ICmd> matching;

  public CmdAmbiguousException(ICmd command, String input, Collection<? extends ICmd> matching) {
    super(command, "ambiguous command");
    this.input = input;
    this.matching = ImmutableList.copyOf(matching);
  }

  public String getInput() {
    return input;
  }

  public Collection<? extends ICmd> getMatching() {
    return matching;
  }
}
