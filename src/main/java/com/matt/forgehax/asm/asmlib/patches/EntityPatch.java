package com.matt.forgehax.asm.asmlib.patches;

import com.matt.forgehax.ForgeHax;
import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.events.replacementhooks.LivingUpdateEvent;
import com.matt.forgehax.asm.utils.ASMHelper;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.futureclient.asm.transformer.AsmMethod;
import net.futureclient.asm.transformer.annotation.Inject;
import net.futureclient.asm.transformer.annotation.Transformer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import org.objectweb.asm.tree.*;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

@Transformer(Entity.class)
public class EntityPatch {

    @Inject(name = "applyEntityCollision", args = {Entity.class},
    description = "Add hook to disable push motion"
    )
    public void applyEntityCollision(MethodNode main) {
        AbstractInsnNode thisEntityPreNode = ASMHelper.findPattern(main.instructions.getFirst(), new int[] {ALOAD, DLOAD, DNEG, DCONST_0, DLOAD, DNEG, INVOKEVIRTUAL}, "xxxxxxx");
        // start at preNode, and scan for next INVOKEVIRTUAL sig
        AbstractInsnNode thisEntityPostNode = ASMHelper.findPattern(thisEntityPreNode, new int[] {INVOKEVIRTUAL}, "x");
        AbstractInsnNode otherEntityPreNode = ASMHelper.findPattern(thisEntityPostNode, new int[] {ALOAD, DLOAD, DCONST_0, DLOAD, INVOKEVIRTUAL}, "xxxxx");
        // start at preNode, and scan for next INVOKEVIRTUAL sig
        AbstractInsnNode otherEntityPostNode = ASMHelper.findPattern(otherEntityPreNode, new int[] {INVOKEVIRTUAL}, "x");

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
        insnThisPre.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onApplyCollisionMotion));
        insnThisPre.add(new JumpInsnNode(IFNE, endJumpForThis));

        InsnList insnOtherPre = new InsnList();
        insnOtherPre.add(new VarInsnNode(ALOAD, 1)); // push entityIn
        insnOtherPre.add(new VarInsnNode(ALOAD, 0)); // push THIS
        insnOtherPre.add(new VarInsnNode(DLOAD, 2)); // push X
        insnOtherPre.add(new VarInsnNode(DLOAD, 4)); // push Z
        insnOtherPre.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onApplyCollisionMotion));
        insnOtherPre.add(new JumpInsnNode(IFNE, endJumpForOther));

        main.instructions.insertBefore(thisEntityPreNode, insnThisPre);
        main.instructions.insert(thisEntityPostNode, endJumpForThis);

        main.instructions.insertBefore(otherEntityPreNode, insnOtherPre);
        main.instructions.insert(otherEntityPostNode, endJumpForOther);
    }

    @Inject(name = "move", args = {MoverType.class, double.class, double.class, double.class},
    description = "Insert flag into statement that performs sneak movement"
    )
    public void moveEntity(MethodNode main) {
        AbstractInsnNode sneakFlagNode = ASMHelper.findPattern(main.instructions.getFirst(), new int[] {
                IFEQ, ALOAD, INSTANCEOF, IFEQ,
                0x00, 0x00,
                LDC, DSTORE
        }, "xxxx??xx");

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
        insnList.add(new JumpInsnNode(IFNE, orJump)); // if not equal, jump past the ForgeHaxHooks.isSafeWalkActivated
        insnList.add(ASMHelper.call(GETSTATIC, TypesHook.Fields.ForgeHaxHooks_isSafeWalkActivated));
        insnList.add(new JumpInsnNode(IFEQ, jumpToLabel));
        insnList.add(orJump);

        AbstractInsnNode previousNode = sneakFlagNode.getPrevious();
        main.instructions.remove(sneakFlagNode); // delete IFEQ
        main.instructions.insert(previousNode, insnList); // insert new instructions
    }

    @Inject(name = "doBlockCollisions",
    description = "Add hook to disable block motion effects"
    )
    public void doBlockCollisions(MethodNode main) {
        AbstractInsnNode preNode = ASMHelper.findPattern(main.instructions.getFirst(), new int[] {
                ASTORE,
                0x00, 0x00,
                ALOAD, INVOKEINTERFACE, ALOAD, GETFIELD, ALOAD, ALOAD, ALOAD, INVOKEVIRTUAL
        }, "x??xxxxxxxx");
        AbstractInsnNode postNode = ASMHelper.findPattern(preNode, new int[] {GOTO}, "x");

        Objects.requireNonNull(preNode, "Find pattern failed for preNode");
        Objects.requireNonNull(postNode, "Find pattern failed for postNode");

        LabelNode endJump = new LabelNode();

        InsnList insnList = new InsnList();
        insnList.add(new VarInsnNode(ALOAD, 0)); // push entity
        insnList.add(new VarInsnNode(ALOAD, 8)); // push block state
        insnList.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_isBlockFiltered));
        insnList.add(new JumpInsnNode(IFNE, endJump));

        main.instructions.insertBefore(postNode, endJump);
        main.instructions.insert(preNode, insnList);
    }

    @Inject(name = "onUpdate")
    public void onUpdateHook(AsmMethod method) {
        final AbstractInsnNode ret = ASMHelper.findPattern(method.method.instructions.getFirst(), new int[] {
            RETURN
        }, "x");

        method.setCursor(ret);
        method.visitInsn(new VarInsnNode(ALOAD, 0)); // this
        method.<Consumer<Entity>>invoke(entity -> {
            if (entity instanceof EntityLivingBase)
                ForgeHax.EVENT_BUS.post(new LivingUpdateEvent((EntityLivingBase)entity));
        });
        //method.returnIf(true);
    }
}
