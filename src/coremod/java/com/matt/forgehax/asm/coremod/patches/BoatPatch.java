package com.matt.forgehax.asm.coremod.patches;

import com.matt.forgehax.asm.coremod.TypesHook;
import com.matt.forgehax.asm.coremod.transformer.RegisterTransformer;
import com.matt.forgehax.asm.coremod.transformer.Transformer;
import com.matt.forgehax.asm.coremod.utils.ASMHelper;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Set;

public class BoatPatch {

    @RegisterTransformer
    public static class UpdateMotion implements Transformer<MethodNode> {
        @Nonnull
        @Override
        public Set<ITransformer.Target> targets() {
            return ASMHelper.getTargetSet(Methods.EntityBoat_updateMotion);
        }

        @Nonnull
        @Override
        public MethodNode transform(MethodNode main, ITransformerVotingContext context) {
            AbstractInsnNode gravityNode =
                ASMHelper.findPattern(
                    main.instructions.getFirst(),
                    new int[] {Opcodes.ALOAD, Opcodes.DUP, Opcodes.GETFIELD, Opcodes.DLOAD, Opcodes.DADD, Opcodes.PUTFIELD},
                    "xxxxxx");

            Objects.requireNonNull(gravityNode, "Find pattern failed for gravityNode");

            AbstractInsnNode putFieldNode = gravityNode;
            for (int i = 0; i < 5; i++) putFieldNode = putFieldNode.getNext();

            LabelNode newLabelNode = new LabelNode();

            InsnList insnList = new InsnList();
            insnList.add(
                ASMHelper.call(Opcodes.GETSTATIC, TypesHook.Fields.ForgeHaxHooks_isNoBoatGravityActivated));
            insnList.add(new JumpInsnNode(Opcodes.IFNE, newLabelNode)); // if nogravity is enabled

            main.instructions.insertBefore(gravityNode, insnList); // insert if
            main.instructions.insert(putFieldNode, newLabelNode); // end if
            return main;
        }
    }

    @RegisterTransformer
    public static class ControlBoat implements Transformer<MethodNode> {
        @Nonnull
        @Override
        public Set<ITransformer.Target> targets() {
            return ASMHelper.getTargetSet(Methods.EntityBoat_controlBoat);
        }

        @Nonnull
        @Override
        public MethodNode transform(MethodNode main, ITransformerVotingContext context) {
            AbstractInsnNode rotationLeftNode =
                ASMHelper.findPattern(
                    main.instructions.getFirst(),
                    new int[] {Opcodes.ALOAD, Opcodes.DUP, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.FADD, Opcodes.PUTFIELD},
                    "xxxxxx");
            AbstractInsnNode rotationRightNode =
                ASMHelper.findPattern(
                    main.instructions.getFirst(),
                    new int[] {Opcodes.ALOAD, Opcodes.DUP, Opcodes.GETFIELD, Opcodes.FCONST_1, Opcodes.FADD, Opcodes.PUTFIELD},
                    "xxxxxx");

            Objects.requireNonNull(rotationLeftNode, "Find pattern failed for leftNode");
            Objects.requireNonNull(rotationRightNode, "Find pattern failed for rightNode");

            AbstractInsnNode putFieldNodeLeft = rotationLeftNode; // get last instruction for left
            for (int i = 0; i < 5; i++) putFieldNodeLeft = putFieldNodeLeft.getNext();

            AbstractInsnNode putFieldNodeRight = rotationRightNode; // get last instruction for right
            for (int i = 0; i < 5; i++) putFieldNodeRight = putFieldNodeRight.getNext();

            /*
             * disable updating deltaRotation for strafing left
             */
            LabelNode newLabelNodeLeft = new LabelNode();

            InsnList insnListLeft = new InsnList();
            insnListLeft.add(
                ASMHelper.call(Opcodes.GETSTATIC, TypesHook.Fields.ForgeHaxHooks_isBoatSetYawActivated));
            insnListLeft.add(new JumpInsnNode(Opcodes.IFNE, newLabelNodeLeft)); // if nogravity is enabled

            main.instructions.insertBefore(rotationLeftNode, insnListLeft); // insert if
            main.instructions.insert(putFieldNodeLeft, newLabelNodeLeft); // end if

            /*
             * disable updating deltaRotation for strafing right
             */
            LabelNode newLabelNodeRight = new LabelNode();

            InsnList insnListRight = new InsnList();
            insnListRight.add(
                ASMHelper.call(Opcodes.GETSTATIC, TypesHook.Fields.ForgeHaxHooks_isBoatSetYawActivated));
            insnListRight.add(new JumpInsnNode(Opcodes.IFNE, newLabelNodeRight)); // if nogravity is enabled

            main.instructions.insertBefore(rotationRightNode, insnListRight); // insert if
            main.instructions.insert(putFieldNodeRight, newLabelNodeRight); // end if
            return main;
        }
    }

    @RegisterTransformer
    public static class RemoveClamp implements Transformer<MethodNode> {
        @Nonnull
        @Override
        public Set<ITransformer.Target> targets() {
            return ASMHelper.getTargetSet(Methods.EntityBoat_applyYawToEntity);
        }

        @Nonnull
        @Override
        public MethodNode transform(MethodNode main, ITransformerVotingContext context) {
            AbstractInsnNode pre =
                ASMHelper.findPattern(
                    main.instructions.getFirst(),
                    new int[] {Opcodes.FLOAD, Opcodes.LDC, Opcodes.LDC, Opcodes.INVOKESTATIC, Opcodes.FSTORE},
                    "xxxxx");
            AbstractInsnNode post = pre.getNext().getNext().getNext(); // INVOKESTATIC

            Objects.requireNonNull(pre, "Find pattern failed for clamp node");
            Objects.requireNonNull(post, "Find pattern failed for clamp node post");

            InsnList insnList = new InsnList();

            LabelNode jump = new LabelNode();

            insnList.add(ASMHelper.call(Opcodes.GETSTATIC, TypesHook.Fields.ForgeHaxHooks_isNoClampingActivated));
            insnList.add(new JumpInsnNode(Opcodes.IFNE, jump)); // if nogravity is enabled

            main.instructions.insert(pre, insnList);
            main.instructions.insert(post, jump);
            return main;
        }
    }
}
