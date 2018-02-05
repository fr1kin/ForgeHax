package com.matt.forgehax.util.command.v2;

import java.util.Collection;

/**
 * Created on 12/25/2017 by fr1kin
 */
public interface IParentCommandV2 extends ICommandV2 {
    Collection<ICommandV2> getChildren();
    Collection<ICommandV2> getChildrenDeep();

    boolean addChild(ICommandV2 command);
    boolean removeChild(ICommandV2 command);
    CommandBuilderV2 makeChild();
}
