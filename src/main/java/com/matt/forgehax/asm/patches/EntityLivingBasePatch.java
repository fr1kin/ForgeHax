package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.helper.AsmMethod;
import com.matt.forgehax.asm.helper.ClassTransformer;
import org.objectweb.asm.tree.MethodNode;

public class EntityLivingBasePatch extends ClassTransformer {

    private final AsmMethod MOVE_ENTITY_WITH_HEADING = new AsmMethod()
            .setName("moveEntityWithHeading")
            .setObfuscatedName("g")
            .setArgumentTypes(float.class, float.class)
            .setReturnType(void.class)
            .setHooks();

    public EntityLivingBasePatch() {
        registerHook(MOVE_ENTITY_WITH_HEADING);
    }

    @Override
    public boolean onTransformMethod(MethodNode method) {
        return false;
    }
}
