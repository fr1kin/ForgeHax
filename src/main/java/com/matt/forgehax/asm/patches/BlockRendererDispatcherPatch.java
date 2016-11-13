package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.helper.ClassTransformer;
import org.objectweb.asm.tree.MethodNode;

/**
 * Created on 11/10/2016 by fr1kin
 */
public class BlockRendererDispatcherPatch extends ClassTransformer {
    @Override
    public boolean onTransformMethod(MethodNode method) {
        return false;
    }
}
