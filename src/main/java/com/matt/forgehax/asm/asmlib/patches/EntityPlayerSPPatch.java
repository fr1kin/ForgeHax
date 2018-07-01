package com.matt.forgehax.asm.asmlib.patches;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.utils.ASMHelper;
import net.futureclient.asm.transformer.annotation.Inject;
import net.futureclient.asm.transformer.annotation.Transformer;
import net.minecraft.client.entity.EntityPlayerSP;
import org.objectweb.asm.tree.*;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

@Transformer(EntityPlayerSP.class)
public class EntityPlayerSPPatch {

    @Inject(name = "onLivingUpdate",
    description = "Add hook to disable the use slowdown effect"
    )
    public void applyLivingUpdate(MethodNode main) {
        AbstractInsnNode applySlowdownSpeedNode = ASMHelper.findPattern(main.instructions.getFirst(), new int[] {
                IFNE,
                0x00, 0x00,
                ALOAD, GETFIELD, DUP, GETFIELD, LDC, FMUL, PUTFIELD
        }, "x??xxxxxxx");

        Objects.requireNonNull(applySlowdownSpeedNode, "Find pattern failed for applySlowdownSpeedNode");

        // get label it jumps to
        LabelNode jumpTo = ((JumpInsnNode) applySlowdownSpeedNode).label;

        InsnList insnList = new InsnList();
        insnList.add(ASMHelper.call(GETSTATIC, TypesHook.Fields.ForgeHaxHooks_isNoSlowDownActivated));
        insnList.add(new JumpInsnNode(IFNE, jumpTo));

        main.instructions.insert(applySlowdownSpeedNode, insnList);
    }

    @Inject(name = "onUpdateWalkingPlayer",
    description = "Add hooks at top and bottom of method"
    )
    public void onUpdateWalkingPlayer(MethodNode main) {
        AbstractInsnNode top = main.instructions.getFirst();
        AbstractInsnNode bottom = ASMHelper.findPattern(main.instructions.getFirst(), new int[] {
                RETURN
        }, "x");

        Objects.requireNonNull(top, "Find pattern failed for top node");
        Objects.requireNonNull(bottom, "Find pattern failed for bottom node");

        InsnList pre = new InsnList();
        pre.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onUpdateWalkingPlayerPre));

        InsnList post = new InsnList();
        post.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onUpdateWalkingPlayerPost));

        main.instructions.insertBefore(top, pre);
        main.instructions.insertBefore(bottom, post);
    }

    @Inject(name = "pushOutOfBlocks", args = {double.class, double.class, double.class}, ret = boolean.class,
    description = "Add hook to disable pushing out of blocks"
    )
    public void pushOutOfBlocks(MethodNode main) {
        AbstractInsnNode preNode = main.instructions.getFirst();
        AbstractInsnNode postNode = ASMHelper.findPattern(main.instructions.getFirst(), new int[] {
                        ICONST_0, IRETURN},
                "xx");

        Objects.requireNonNull(preNode, "Find pattern failed for pre node");
        Objects.requireNonNull(postNode, "Find pattern failed for post node");

        LabelNode endJump = new LabelNode();

        InsnList insnPre = new InsnList();
        insnPre.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onPushOutOfBlocks));
        insnPre.add(new JumpInsnNode(IFNE, endJump));

        main.instructions.insertBefore(preNode, insnPre);
        main.instructions.insertBefore(postNode, endJump);
    }

    @Inject(name = "isRowingBoat", ret = boolean.class,
    description = "Add hook to override returned value of isRowingBoat"
    )
    public void rowingBoat(MethodNode main) {
        AbstractInsnNode preNode = main.instructions.getFirst();

        Objects.requireNonNull(preNode, "Find pattern failed for pre node");

        LabelNode jump = new LabelNode();

        InsnList insnPre = new InsnList();
        //insnPre.add(ASMHelper.call(GETSTATIC, TypesHook.Fields.ForgeHaxHooks_isNotRowingBoatActivated));
        //insnPre.add(new JumpInsnNode(IFEQ, jump));

        insnPre.add(new InsnNode(ICONST_0));
        insnPre.add(new InsnNode(IRETURN)); // return false
        //insnPre.add(jump);


        main.instructions.insert(insnPre);
    }
}
