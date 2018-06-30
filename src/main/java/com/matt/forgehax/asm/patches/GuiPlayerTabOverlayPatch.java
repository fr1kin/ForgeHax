package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.utils.ASMHelper;
import com.matt.forgehax.asm.utils.asmtype.ASMMethod;
import com.matt.forgehax.asm.utils.transforming.ClassTransformer;
import com.matt.forgehax.asm.utils.transforming.Inject;
import com.matt.forgehax.asm.utils.transforming.MethodTransformer;
import com.matt.forgehax.asm.utils.transforming.RegisterMethodTransformer;
import org.objectweb.asm.tree.*;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

public class GuiPlayerTabOverlayPatch extends ClassTransformer {
    public GuiPlayerTabOverlayPatch() {
        super(Classes.GuiPlayerTabOverlay);
    }

    @RegisterMethodTransformer
    private class RenderPlayerList extends MethodTransformer {
        @Override
        public ASMMethod getMethod() {
            return Methods.GuiTabOverlay_renderPlayerlist;
        }

        @Inject(description = "Add hook that disables rendering of player faces in player list")
        public void inject(MethodNode main) {
            AbstractInsnNode node = ASMHelper.findPattern(main.instructions.getFirst(), new int[] {
                    ALOAD, 0x00, 0x00, IFNE,
                    /*ALOAD, 0x00, 0x00,0x00,0x00, IFEQ, ICONST_1, GOTO, ICONST_0, ISTORE*/ // doesnt work???
            }, /*"x??x*x????xxxxx"*/"x??x");

            Objects.requireNonNull(node);

            AbstractInsnNode store = node;
            for (int i = 0; i < 13; i++) {
                store = store.getNext();
            }

            LabelNode jump = new LabelNode();

            InsnList list = new InsnList();
            list.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onPlayerList));
            list.add(new JumpInsnNode(IFNE, jump));

            main.instructions.insertBefore(node, list);
            main.instructions.insert(store, jump);
        }

    }
}
