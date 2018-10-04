package com.matt.forgehax.util.command.v2;

import java.util.Collection;

/** Created on 12/25/2017 by fr1kin */
public interface IParentCmd extends ICmd {
  Collection<ICmd> getChildren();

  Collection<ICmd> getChildrenDeep();

  boolean addChild(ICmd command);

  boolean removeChild(ICmd command);

  CmdBuilders makeChild();

  ICmd findChild(final String name);
}
