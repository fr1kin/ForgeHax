package com.matt.forgehax.asm.asmlib.patches;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.utils.ASMHelper;
import net.futureclient.asm.transformer.annotation.Inject;
import net.futureclient.asm.transformer.annotation.Transformer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import org.objectweb.asm.tree.*;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

@Transformer(EntityBoat.class)
public class BoatPatch {

    @Inject(name = "updateMotion",
            description = "Add hook to disable boat gravity"
    )
    public void updateMotion(MethodNode main) {
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

    @Inject(name = "controlBoat",
            description = "Add hooks to disable boat rotation"
    )
    public void controlBoat(MethodNode main) {
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
        insnListLeft.add(ASMHelper.call(GETSTATIC, TypesHook.Fields.ForgeHaxHooks_isBoatSetYawActivated));
        insnListLeft.add(new JumpInsnNode(IFNE, newLabelNodeLeft)); // if nogravity is enabled

        main.instructions.insertBefore(rotationLeftNode, insnListLeft); // insert if
        main.instructions.insert(putFieldNodeLeft, newLabelNodeLeft); // end if

        /*
         * disable updating deltaRotation for strafing right
         */
        LabelNode newLabelNodeRight = new LabelNode();

        InsnList insnListRight = new InsnList();
        insnListRight.add(ASMHelper.call(GETSTATIC, TypesHook.Fields.ForgeHaxHooks_isBoatSetYawActivated));
        insnListRight.add(new JumpInsnNode(IFNE, newLabelNodeRight)); // if nogravity is enabled

        main.instructions.insertBefore(rotationRightNode, insnListRight); // insert if
        main.instructions.insert(putFieldNodeRight, newLabelNodeRight); // end if
    }

    @Inject(name = "applyYawToEntity", args = {Entity.class},
    description = "Disable boat view clamping"
    )
    public void removeClamp(MethodNode main) {
        AbstractInsnNode pre = ASMHelper.findPattern(main.instructions.getFirst(), new int[] {
                FLOAD, LDC, LDC, INVOKESTATIC, FSTORE
        }, "xxxxx");
        AbstractInsnNode post = pre.getNext().getNext().getNext(); // INVOKESTATIC

        Objects.requireNonNull(pre, "Find pattern failed for clamp node");
        Objects.requireNonNull(post, "Find pattern failed for clamp node post");

        InsnList insnList = new InsnList();

        LabelNode jump = new LabelNode();

        insnList.add(ASMHelper.call(GETSTATIC, TypesHook.Fields.ForgeHaxHooks_isNoClampingActivated));
        insnList.add(new JumpInsnNode(IFNE, jump)); // if nogravity is enabled

        main.instructions.insert(pre, insnList);
        main.instructions.insert(post, jump);
    }
}
