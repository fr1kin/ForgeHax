package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.utils.ASMHelper;
import com.matt.forgehax.asm.utils.asmtype.ASMMethod;
import com.matt.forgehax.asm.utils.transforming.ClassTransformer;
import com.matt.forgehax.asm.utils.transforming.Inject;
import com.matt.forgehax.asm.utils.transforming.MethodTransformer;
import com.matt.forgehax.asm.utils.transforming.RegisterMethodTransformer;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.PrintStream;
import java.util.ListIterator;
import java.util.Objects;

import static org.objectweb.asm.Type.getInternalName;
import static org.objectweb.asm.Opcodes.*;

/**
 * Created by Babbaj on 2/14/2018
 */
public class SkinManagerPatch extends ClassTransformer {
    public SkinManagerPatch() {
        super(Classes.SkinManager$3);
    }

    @RegisterMethodTransformer
    private class Run extends MethodTransformer {
        @Override
        public ASMMethod getMethod() {
            return Methods.SkinManager$3_run;
        }

        @Inject(description = "Modify SkinManager::loadProfileTextures to fix lag player list lag")
        public void inject(MethodNode main) {
            AbstractInsnNode pre = ASMHelper.findPattern(main.instructions.getFirst(), new int[] {
                    INVOKESTATIC, NEW, DUP, ALOAD, ALOAD, INVOKESPECIAL, INVOKEVIRTUAL
            }, "xxxxxxx");


            Objects.requireNonNull(pre);

            AbstractInsnNode post = pre;
            for (int i = 0; i < 6; i++)
                post = post.getNext();


            pre = pre.getNext();
            main.instructions.remove(pre.getPrevious()); // remove getMinecraft()
            post = post.getNext();
            main.instructions.remove(post.getPrevious()); // remove addScheduledTask()
            post = post.getNext();
            main.instructions.remove(post.getPrevious()); // remove pop


            MethodInsnNode runnable_run = new MethodInsnNode(INVOKEINTERFACE, getInternalName(Runnable.class), "run", "()V", true);
            main.instructions.insertBefore(post, runnable_run);
        }
    }
}
