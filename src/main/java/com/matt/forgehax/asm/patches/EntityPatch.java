package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.helper.AsmHelper;
import com.matt.forgehax.asm.helper.AsmMethod;
import com.matt.forgehax.asm.helper.transforming.ClassTransformer;
import com.matt.forgehax.asm.helper.transforming.Inject;
import com.matt.forgehax.asm.helper.transforming.MethodTransformer;
import com.matt.forgehax.asm.helper.transforming.RegisterPatch;
import org.objectweb.asm.tree.*;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

public class EntityPatch extends ClassTransformer {
    public final AsmMethod APPLY_ENTITY_COLLISION = new AsmMethod()
            .setName("applyEntityCollision")
            .setObfuscatedName("i")
            .setArgumentTypes(NAMES.ENTITY)
            .setReturnType(void.class)
            .setHooks(NAMES.ON_APPLY_COLLISION);

    public final AsmMethod MOVE_ENTITY = new AsmMethod()
            .setName("move")
            .setObfuscatedName("a")
            .setArgumentTypes(NAMES.MOVERTYPE, double.class, double.class, double.class)
            .setReturnType(void.class)
            .setHooks(NAMES.ON_WEB_MOTION);

    public final AsmMethod DO_APPLY_COLLISIONS = new AsmMethod()
            .setName("doBlockCollisions")
            .setObfuscatedName("ac")
            .setArgumentTypes()
            .setReturnType(void.class)
            .setHooks(NAMES.ON_DO_BLOCK_COLLISIONS);

    public EntityPatch() {
        super("net/minecraft/entity/Entity");
    }

    @RegisterPatch
    private class ApplyEntityCollision extends MethodTransformer {
        @Override
        public AsmMethod getMethod() {
            return APPLY_ENTITY_COLLISION;
        }

        @Inject
        private void inject(MethodNode main) {
            AbstractInsnNode thisEntityPreNode = AsmHelper.findPattern(main.instructions.getFirst(), new int[] {ALOAD, DLOAD, DNEG, DCONST_0, DLOAD, DNEG, INVOKEVIRTUAL}, "xxxxxxx");
            // start at preNode, and scan for next INVOKEVIRTUAL sig
            AbstractInsnNode thisEntityPostNode = AsmHelper.findPattern(thisEntityPreNode, new int[] {INVOKEVIRTUAL}, "x");
            AbstractInsnNode otherEntityPreNode = AsmHelper.findPattern(thisEntityPostNode, new int[] {ALOAD, DLOAD, DCONST_0, DLOAD, INVOKEVIRTUAL}, "xxxxx");
            // start at preNode, and scan for next INVOKEVIRTUAL sig
            AbstractInsnNode otherEntityPostNode = AsmHelper.findPattern(otherEntityPreNode, new int[] {INVOKEVIRTUAL}, "x");

            Objects.requireNonNull(thisEntityPreNode, "Find pattern failed for thisEntityPreNode");
            Objects.requireNonNull(thisEntityPostNode, "Find pattern failed for thisEntityPostNode");
            Objects.requireNonNull(otherEntityPreNode, "Find pattern failed for otherEntityPreNode");
            Objects.requireNonNull(otherEntityPostNode, "Find pattern failed for otherEntityPostNode");

            LabelNode endJumpForThis = new LabelNode();
            LabelNode endJumpForOther = new LabelNode();

            // first we handle this.addVelocity

            InsnList insnThisPre = new InsnList();
            insnThisPre.add(new VarInsnNode(ALOAD, 0)); // push THIS
            insnThisPre.add(new VarInsnNode(ALOAD, 1));
            insnThisPre.add(new VarInsnNode(DLOAD, 2));
            insnThisPre.add(new InsnNode(DNEG)); // push -X
            insnThisPre.add(new VarInsnNode(DLOAD, 4));
            insnThisPre.add(new InsnNode(DNEG)); // push -Z
            insnThisPre.add(new MethodInsnNode(INVOKESTATIC,
                    NAMES.ON_APPLY_COLLISION.getParentClass().getRuntimeName(),
                    NAMES.ON_APPLY_COLLISION.getRuntimeName(),
                    NAMES.ON_APPLY_COLLISION.getDescriptor(),
                    false
            ));
            insnThisPre.add(new JumpInsnNode(IFNE, endJumpForThis));

            InsnList insnOtherPre = new InsnList();
            insnOtherPre.add(new VarInsnNode(ALOAD, 1)); // push entityIn
            insnOtherPre.add(new VarInsnNode(ALOAD, 0)); // push THIS
            insnOtherPre.add(new VarInsnNode(DLOAD, 2)); // push X
            insnOtherPre.add(new VarInsnNode(DLOAD, 4)); // push Z
            insnOtherPre.add(new MethodInsnNode(INVOKESTATIC,
                    NAMES.ON_APPLY_COLLISION.getParentClass().getRuntimeName(),
                    NAMES.ON_APPLY_COLLISION.getRuntimeName(),
                    NAMES.ON_APPLY_COLLISION.getDescriptor(),
                    false
            ));
            insnOtherPre.add(new JumpInsnNode(IFNE, endJumpForOther));

            main.instructions.insertBefore(thisEntityPreNode, insnThisPre);
            main.instructions.insert(thisEntityPostNode, endJumpForThis);

            main.instructions.insertBefore(otherEntityPreNode, insnOtherPre);
            main.instructions.insert(otherEntityPostNode, endJumpForOther);
        }
    }

    @RegisterPatch
    private class MoveEntity extends MethodTransformer {
        @Override
        public AsmMethod getMethod() {
            return MOVE_ENTITY;
        }

        @Inject
        public void inject(MethodNode main) {
            AbstractInsnNode sneakFlagNode = AsmHelper.findPattern(main.instructions.getFirst(), new int[] {
                    IFEQ, ALOAD, INSTANCEOF, IFEQ,
                    0x00, 0x00,
                    LDC, DSTORE
            }, "xxxx??xx");

            Objects.requireNonNull(sneakFlagNode, "Find pattern failed for sneakFlagNode");

            // the original label to the jump
            LabelNode jumpToLabel = ((JumpInsnNode) sneakFlagNode).label;
            // the or statement jump if isSneaking returns false
            LabelNode orJump = new LabelNode();

            InsnList insnList = new InsnList();
            insnList.add(new JumpInsnNode(IFNE, orJump)); // if not equal, jump past the ForgeHaxHooks.isSafeWalkActivated
            insnList.add(new FieldInsnNode(GETSTATIC,
                    NAMES.IS_SAFEWALK_ACTIVE.getParentClass().getRuntimeName(),
                    NAMES.IS_SAFEWALK_ACTIVE.getRuntimeName(),
                    NAMES.IS_SAFEWALK_ACTIVE.getTypeDescriptor()
            ));// get the value of isSafeWalkActivated
            insnList.add(new JumpInsnNode(IFEQ, jumpToLabel));
            insnList.add(orJump);

            AbstractInsnNode previousNode = sneakFlagNode.getPrevious();
            main.instructions.remove(sneakFlagNode); // delete IFEQ
            main.instructions.insert(previousNode, insnList); // insert new instructions
        }
    }

    @RegisterPatch
    private class DoBlockCollisions extends MethodTransformer {
        @Override
        public AsmMethod getMethod() {
            return DO_APPLY_COLLISIONS;
        }

        @Inject
        public void inject(MethodNode main) {
            AbstractInsnNode preNode = AsmHelper.findPattern(main.instructions.getFirst(), new int[] {
                    ASTORE,
                    0x00, 0x00,
                    ALOAD, INVOKEINTERFACE, ALOAD, GETFIELD, ALOAD, ALOAD, ALOAD, INVOKEVIRTUAL
            }, "x??xxxxxxxx");
            AbstractInsnNode postNode = AsmHelper.findPattern(preNode, new int[] {GOTO}, "x");

            Objects.requireNonNull(preNode, "Find pattern failed for preNode");
            Objects.requireNonNull(postNode, "Find pattern failed for postNode");

            LabelNode endJump = new LabelNode();

            InsnList insnList = new InsnList();
            insnList.add(new VarInsnNode(ALOAD, 0)); // push entity
            insnList.add(new VarInsnNode(ALOAD, 8)); // push block state
            insnList.add(new MethodInsnNode(INVOKESTATIC,
                    NAMES.IS_BLOCK_COLLISION_FILTERED.getParentClass().getRuntimeName(),
                    NAMES.IS_BLOCK_COLLISION_FILTERED.getRuntimeName(),
                    NAMES.IS_BLOCK_COLLISION_FILTERED.getDescriptor(),
                    false
            ));
            insnList.add(new JumpInsnNode(IFNE, endJump));

            main.instructions.insertBefore(postNode, endJump);
            main.instructions.insert(preNode, insnList);
        }
    }
}
