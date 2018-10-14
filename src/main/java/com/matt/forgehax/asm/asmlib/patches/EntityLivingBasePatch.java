package com.matt.forgehax.asm.asmlib.patches;

import com.matt.forgehax.ForgeHax;
import com.matt.forgehax.asm.events.replacementhooks.LivingUpdateEvent;
import java.util.function.Consumer;
import net.futureclient.asm.transformer.AsmMethod;
import net.futureclient.asm.transformer.annotation.Inject;
import net.futureclient.asm.transformer.annotation.Transformer;
import net.minecraft.entity.EntityLivingBase;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.function.Predicate;

import static org.objectweb.asm.Opcodes.*;

//@Transformer(EntityLivingBase.class)
// TODO: fix
public class EntityLivingBasePatch {

    @Inject(name = "onUpdate")
    public void updateHook(AsmMethod method) {
        method.visitInsn(new VarInsnNode(ALOAD, 0)); // this
        method.<Predicate<EntityLivingBase>>invoke(entity ->
                ForgeHax.EVENT_BUS.post(new LivingUpdateEvent(entity))
        );
        method.returnIf(true);
    }
}
