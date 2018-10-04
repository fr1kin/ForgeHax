package com.matt.forgehax.util.command.v2;

import javax.annotation.Nullable;

public class ParentCmdBuilder extends AbstractCmdBuilder<ParentCmdBuilder, IParentCmd> {
  public ParentCmdBuilder(@Nullable IParentCmd parent) {
    super(parent);
  }

  @Override
  public IParentCmd build() {
    return CmdHelper.registerAll(
        new ParentCmd(name, aliases, description, parent, options), callbacks, flags);
  }
}
