package com.matt.forgehax.asm.helper;

import org.objectweb.asm.tree.ClassNode;

public interface IAsmTransformer {
    boolean transform(ClassNode node);
}
