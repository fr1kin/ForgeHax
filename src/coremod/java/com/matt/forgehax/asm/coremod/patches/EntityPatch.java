package com.matt.forgehax.asm.coremod.patches;

import com.matt.forgehax.asm.coremod.TypesHook;
import com.matt.forgehax.asm.coremod.transformer.RegisterTransformer;
import com.matt.forgehax.asm.coremod.transformer.Transformer;
import com.matt.forgehax.asm.coremod.utils.ASMHelper;
import com.matt.forgehax.asm.coremod.utils.AsmPattern;
import com.matt.forgehax.asm.coremod.utils.InsnPattern;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Set;

public class EntityPatch {

    @RegisterTransformer
    public static class ApplyEntityCollision implements Transformer<MethodNode> {
        @Nonnull
        @Override
        public Set<ITransformer.Target> targets() {
            return ASMHelper.getTargetSet(Methods.Entity_applyEntityCollision);
        }

        @Nonnull
        @Override
        public MethodNode transform(MethodNode main, ITransformerVotingContext context) {
            AsmPattern isBeingRiddenCheck = new AsmPattern.Builder(AsmPattern.CODE_ONLY)
                .opcode(Opcodes.ALOAD)
                .ASMType(Opcodes.INVOKEVIRTUAL, Methods.Entity_isBeingRidden) // TODO: maybe dont use isBeingRidden
                .opcodes(Opcodes.IFNE)
                .build();

            InsnPattern addVelocityThis = isBeingRiddenCheck.test(main);

            LabelNode jumpThis = addVelocityThis.<JumpInsnNode>getLast().label;
            InsnList hookThis = new InsnList();
            hookThis.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
            hookThis.add(new VarInsnNode(Opcodes.ALOAD, 1)); // other
            hookThis.add(new VarInsnNode(Opcodes.DLOAD, 2));
            hookThis.add(new InsnNode(Opcodes.DNEG)); // push -X
            hookThis.add(new VarInsnNode(Opcodes.DLOAD, 4));
            hookThis.add(new InsnNode(Opcodes.DNEG)); // push -Z
            hookThis.add(ASMHelper.call(Opcodes.INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onApplyCollisionMotion));
            hookThis.add(new JumpInsnNode(Opcodes.IFNE, jumpThis));


            InsnPattern addVelocityOther = isBeingRiddenCheck.test(addVelocityThis.getLast());

            LabelNode jumpOther = addVelocityOther.<JumpInsnNode>getLast().label;
            InsnList hookOther = new InsnList();
            hookOther.add(new VarInsnNode(Opcodes.ALOAD, 1)); // other
            hookOther.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
            hookOther.add(new VarInsnNode(Opcodes.DLOAD, 2)); // push X
            hookOther.add(new VarInsnNode(Opcodes.DLOAD, 4)); // push Z
            hookOther.add(ASMHelper.call(Opcodes.INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onApplyCollisionMotion));
            hookOther.add(new JumpInsnNode(Opcodes.IFNE, jumpOther));

            main.instructions.insert(addVelocityThis.getLast(), hookThis);
            main.instructions.insert(addVelocityOther.getLast(), hookOther);

            return main;
        }
    }

    @RegisterTransformer
    public static class MoveEntity implements Transformer<MethodNode> {
        @Nonnull
        @Override
        public Set<ITransformer.Target> targets() {
            return ASMHelper.getTargetSet(Methods.Entity_move);
        }

        @Nonnull
        @Override // 2 lazy to rewrite
        public MethodNode transform(MethodNode main, ITransformerVotingContext context) {
            AbstractInsnNode sneakFlagNode =
                ASMHelper.findPattern(
                    main.instructions.getFirst(),
                    new int[] {Opcodes.IFEQ, Opcodes.ALOAD, Opcodes.INSTANCEOF, Opcodes.IFEQ, 0x00, 0x00, Opcodes.LDC, Opcodes.DSTORE},
                    "xxxx??xx");

            Objects.requireNonNull(sneakFlagNode, "Find pattern failed for sneakFlagNode");

            AbstractInsnNode instanceofCheck = sneakFlagNode.getNext();
            for (int i = 0; i < 3; i++) {
                instanceofCheck = instanceofCheck.getNext();
                main.instructions.remove(instanceofCheck.getPrevious());
            }

            // the original label to the jump
            LabelNode jumpToLabel = ((JumpInsnNode) sneakFlagNode).label;
            // the or statement jump if isSneaking returns false
            LabelNode orJump = new LabelNode();

            InsnList insnList = new InsnList();
            insnList.add(new JumpInsnNode(Opcodes.IFNE, orJump)); // if not equal, jump past the ForgeHaxHooks.isSafeWalkActivated
            insnList.add(ASMHelper.call(Opcodes.GETSTATIC, TypesHook.Fields.ForgeHaxHooks_isSafeWalkActivated));
            insnList.add(new JumpInsnNode(Opcodes.IFEQ, jumpToLabel));
            insnList.add(orJump);

            AbstractInsnNode previousNode = sneakFlagNode.getPrevious();
            main.instructions.remove(sneakFlagNode); // delete IFEQ
            main.instructions.insert(previousNode, insnList); // insert new instruction

            return main;
        }
    }

    @RegisterTransformer
    public static class DoBlockCollisions implements Transformer<MethodNode> {
        @Nonnull
        @Override
        public Set<ITransformer.Target> targets() {
            return ASMHelper.getTargetSet(Methods.Entity_doBlockCollisions);
        }

        @Nonnull
        @Override
        public MethodNode transform(MethodNode main, ITransformerVotingContext context) {
            InsnPattern node = new AsmPattern.Builder(AsmPattern.CODE_ONLY)
                .opcodes(Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ALOAD, Opcodes.ALOAD)
                .opcode(Opcodes.INVOKEINTERFACE) // onEntityCollision
                .opcodes(Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.INVOKEVIRTUAL) // onInsideBlock
                .build()
                .test(main);

            LabelNode jump = new LabelNode();

            InsnList list = new InsnList();
            list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
            list.add(new VarInsnNode(Opcodes.ALOAD, 11)); // blockstate
            list.add(ASMHelper.call(Opcodes.INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_isBlockFiltered));
            list.add(new JumpInsnNode(Opcodes.IFNE, jump));

            main.instructions.insert(node.getLast(), jump);
            main.instructions.insertBefore(node.getFirst(), list);

            return main;
        }
    }
}
