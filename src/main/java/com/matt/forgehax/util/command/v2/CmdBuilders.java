package com.matt.forgehax.util.command.v2;

import javax.annotation.Nullable;

public class CmdBuilders {
    private static final CmdBuilders INSTANCE = new CmdBuilders();

    public static CmdBuilders getInstance() {
        return INSTANCE;
    }

    @Nullable
    private final IParentCmd parent;

    public CmdBuilders(IParentCmd parent) {
        this.parent = parent;
    }
    public CmdBuilders() {
        this(null);
    }

    public ParentCmdBuilder newParent() {
        return new ParentCmdBuilder(parent);
    }
}
