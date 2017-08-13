package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.utils.ASMHelper;
import com.matt.forgehax.asm.utils.asmtype.ASMMethod;
import com.matt.forgehax.asm.utils.transforming.ClassTransformer;
import com.matt.forgehax.asm.utils.transforming.MethodTransformer;
import com.matt.forgehax.asm.utils.transforming.RegisterMethodTransformer;
import com.matt.forgehax.asm.utils.transforming.Inject;
import org.objectweb.asm.tree.*;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

public class BoatPatch extends ClassTransformer {
    public BoatPatch() {
        super(Classes.EntityBoat);
    }

    @RegisterMethodTransformer
    private class updateMotion extends MethodTransformer {
        @Override
        public ASMMethod getMethod() {
            return Methods.EntityBoat_updateMotion;
        }

        @Inject(description = "Add hook to disable boat gravity")
        public void inject(MethodNode main) {
            AbstractInsnNode gravityNode = ASMHelper.findPattern(main.instructions.getFirst(), new int[] {
                    ALOAD, DUP, GETFIELD, DLOAD, DADD, PUTFIELD
            }, "xxxxxx");

            Objects.requireNonNull(gravityNode, "Find pattern failed for gravityNode");

            AbstractInsnNode putFieldNode = gravityNode;
            for (int i = 0; i < 5; i++)
                putFieldNode = putFieldNode.getNext();


            LabelNode newLabelNode = new LabelNode();

            InsnList insnList = new InsnList();
            insnList.add(ASMHelper.call(GETSTATIC, TypesHook.Fields.ForgeHaxHooks_isNoBoatGravityActivated));
            insnList.add(new JumpInsnNode(IFNE, newLabelNode)); // if nogravity is enabled

            main.instructions.insertBefore(gravityNode, insnList); // insert if
            main.instructions.insert(putFieldNode, newLabelNode); // end if
        }
    }

    @RegisterMethodTransformer
    private class controlBoat extends MethodTransformer {
        @Override
        public ASMMethod getMethod() {
            return Methods.EntityBoat_controlBoat;
        }

        @Inject(description = "Add hooks to disable boat rotation")
        public void inject(MethodNode main) {
            AbstractInsnNode rotationLeftNode = ASMHelper.findPattern(main.instructions.getFirst(), new int[] {
                    ALOAD, DUP, GETFIELD, LDC, FADD, PUTFIELD
            }, "xxxxxx");
            AbstractInsnNode rotationRightNode = ASMHelper.findPattern(main.instructions.getFirst(), new int[] {
                    ALOAD, DUP, GETFIELD, FCONST_1, FADD, PUTFIELD
            }, "xxxxxx");

            Objects.requireNonNull(rotationLeftNode, "Find pattern failed for leftNode");
            Objects.requireNonNull(rotationRightNode, "Find pattern failed for rightNode");

            AbstractInsnNode putFieldNodeLeft = rotationLeftNode; // get last instruction for left
            for (int i = 0; i < 5; i++)
                putFieldNodeLeft = putFieldNodeLeft.getNext();

            AbstractInsnNode putFieldNodeRight = rotationRightNode;  // get last instruction for right
            for (int i = 0; i < 5; i++)
                putFieldNodeRight = putFieldNodeRight.getNext();

            /*
            * disable updating deltaRotation for strafing left
            */
            LabelNode newLabelNodeLeft = new LabelNode();

            InsnList insnListLeft = new InsnList();
            insnListLeft.add(ASMHelper.call(GETSTATIC, TypesHook.Fields.ForgeHaxHooks_isNoBoatRotationActivated));
            insnListLeft.add(new JumpInsnNode(IFNE, newLabelNodeLeft)); // if nogravity is enabled

            main.instructions.insertBefore(rotationLeftNode, insnListLeft); // insert if
            main.instructions.insert(putFieldNodeLeft, newLabelNodeLeft); // end if

            /*
            * disable updating deltaRotation for strafing right
            */
            LabelNode newLabelNodeRight = new LabelNode();

            InsnList insnListRight = new InsnList();
            insnListRight.add(ASMHelper.call(GETSTATIC, TypesHook.Fields.ForgeHaxHooks_isNoBoatRotationActivated));
            insnListRight.add(new JumpInsnNode(IFNE, newLabelNodeRight)); // if nogravity is enabled

            main.instructions.insertBefore(rotationRightNode, insnListRight); // insert if
            main.instructions.insert(putFieldNodeRight, newLabelNodeRight); // end if
        }
    }

    @RegisterMethodTransformer
    private class removeClamp extends MethodTransformer {
        @Override
        public ASMMethod getMethod() {
            return Methods.EntityBoat_applyYawToEntity;
        }

        @Inject(description = "Delete boat view clamping")
        public void inject(MethodNode main) {
            AbstractInsnNode node = ASMHelper.findPattern(main.instructions.getFirst(), new int[] {
                    FLOAD, LDC, LDC, INVOKESTATIC, FSTORE
            }, "xxxxx");

            Objects.requireNonNull(node, "Find pattern failed for clamp node");


            node = node.getNext(); // go to first LDC
            for (int i = 0; i < 3; i++) { // remove clamping code and make it set 'f1' to 'f'
                node = node.getNext();
                main.instructions.remove(node.getPrevious());
            }

        }
    }
}