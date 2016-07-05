package com.matt.forgehax.asm.helper;

import org.objectweb.asm.tree.MethodNode;

public interface IAsmHook {
    boolean onTransform(MethodNode node);
}
