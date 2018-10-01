package com.matt.forgehax.asm.asmlib.patches;

import com.matt.forgehax.ForgeHax;
import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.events.replacementhooks.GuiScreenEvent;
import com.matt.forgehax.asm.utils.ASMHelper;
import com.matt.forgehax.asm.utils.InsnPattern;
import net.futureclient.asm.transformer.AsmMethod;
import net.futureclient.asm.transformer.annotation.Inject;
import net.futureclient.asm.transformer.annotation.Transformer;
import net.minecraft.client.renderer.EntityRenderer;
import org.objectweb.asm.tree.*;

import java.util.Objects;

import static com.matt.forgehax.Globals.MC;
import static org.objectweb.asm.Opcodes.*;

@Transformer(EntityRenderer.class)
public class EntityRendererPatch {

    @Inject(name = "hurtCameraEffect", args = {float.class},
    description = "Add hook that allows the method to be canceled"
    )
    public void hurtCameraEffect(MethodNode main) {
        AbstractInsnNode preNode = main.instructions.getFirst();
        AbstractInsnNode postNode = ASMHelper.findPattern(main.instructions.getFirst(), new int[] {
                RETURN
        }, "x");

        Objects.requireNonNull(preNode, "Find pattern failed for preNode");
        Objects.requireNonNull(postNode, "Find pattern failed for postNode");

        LabelNode endJump = new LabelNode();

        InsnList insnPre = new InsnList();
        insnPre.add(new VarInsnNode(FLOAD, 1));
        insnPre.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onHurtcamEffect));
        insnPre.add(new JumpInsnNode(IFNE, endJump));

        main.instructions.insertBefore(preNode, insnPre);
        main.instructions.insertBefore(postNode, endJump);
    }

    @Inject(name = "updateCameraAndRender", args = {float.class, long.class},
    description = "Add hook for GuiScreenEvent.DrawScreenEvent")
    public void guiDrawScreenHook(AsmMethod method) {
        InsnPattern node = ASMHelper._findPattern(method.method.instructions.getFirst(), new int[] {
            ALOAD, GETFIELD, GETFIELD, ILOAD, ILOAD, ALOAD, GETFIELD, INVOKEVIRTUAL, 0x00
        }, "xxxxxxxx?"); // forge if last node is invokestatic, vanilla if invokevirtual
        final AbstractInsnNode post = node.getLast().getNext();
        Objects.requireNonNull(node, "Failed to find pattern for node");

        {
            method.setCursor(node.getFirst());
            method.visitInsn(new VarInsnNode(ILOAD, 8)); // k1 (mouseX)
            method.visitInsn(new VarInsnNode(ILOAD, 9)); // l1 (mouseY)
            method.<IntBiPredicate>invoke((mouseX, mouseY) ->
                    ForgeHax.EVENT_BUS.post(new GuiScreenEvent.DrawScreenEvent.Pre(
                            MC.currentScreen,
                            mouseX,
                            mouseY,
                            MC.getTickLength()
                            )
                    )
            );
            method.jumpIf(IFNE, node.getLast().getNext());
        }
        {
            method.setCursor(post);
            method.visitInsn(new VarInsnNode(ILOAD, 8)); // k1 (mouseX)
            method.visitInsn(new VarInsnNode(ILOAD, 9)); // l1 (mouseY)
            method.<IntBiConsumer>invoke((mouseX, mouseY) ->
                    ForgeHax.EVENT_BUS.post(new GuiScreenEvent.DrawScreenEvent.Post(
                            MC.currentScreen,
                            mouseX,
                            mouseY,
                            MC.getTickLength()
                            )
                    )
            );
        }
    }

    private interface IntBiPredicate {
        boolean test(int a, int b);
    }

    private interface IntBiConsumer {
        void accept(int a, int b);
    }
}
