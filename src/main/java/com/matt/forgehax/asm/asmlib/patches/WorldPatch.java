package com.matt.forgehax.asm.asmlib.patches;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.utils.ASMHelper;
import net.futureclient.asm.transformer.annotation.Inject;
import net.futureclient.asm.transformer.annotation.Transformer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import org.objectweb.asm.tree.*;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

@Transformer(World.class)
public class WorldPatch {

    @Inject(name = "handleMaterialAcceleration",
            args = {AxisAlignedBB.class, Material.class, Entity.class},
            ret = boolean.class,
    description = "Add hook that allows water movement math to be skipped"
    )
    public void handleMaterialAcceleration(MethodNode method) {
        AbstractInsnNode preNode = ASMHelper.findPattern(method.instructions.getFirst(), new int[] {
                ALOAD, INVOKEVIRTUAL, ASTORE,
                0x00, 0x00,
                LDC, DSTORE,
                0x00, 0x00,
                ALOAD, DUP, GETFIELD, ALOAD, GETFIELD, LDC, DMUL, DADD, PUTFIELD
        }, "xxx??xx??xxxxxxxxx");
        AbstractInsnNode postNode = ASMHelper.findPattern(method.instructions.getFirst(), new int[] {
                ILOAD, IRETURN
        }, "xx");

        Objects.requireNonNull(preNode, "Find pattern failed for preNode");
        Objects.requireNonNull(postNode, "Find pattern failed for postNode");

        LabelNode endJump = new LabelNode();

        InsnList insnPre = new InsnList();
        insnPre.add(new VarInsnNode(ALOAD, 3));
        insnPre.add(new VarInsnNode(ALOAD, 11));
        insnPre.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onWaterMovement));
        insnPre.add(new JumpInsnNode(IFNE, endJump));

        method.instructions.insertBefore(preNode, insnPre);
        method.instructions.insertBefore(postNode, endJump);
    }


    @Inject(name = "checkLightFor", args = {EnumSkyBlock.class, BlockPos.class}, ret = boolean.class,
    description = "Add hook before everything"
    )
    public void checkLightFor(MethodNode method) {
        AbstractInsnNode node = method.instructions.getFirst();

        Objects.requireNonNull(node, "Failed to find node.");

        LabelNode label = new LabelNode();

        InsnList list = new InsnList();
        list.add(new VarInsnNode(ALOAD, 1)); // enum
        list.add(new VarInsnNode(ALOAD, 2)); // blockpos
        list.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onWorldCheckLightFor));
        list.add(new JumpInsnNode(IFEQ, label));
        list.add(new InsnNode(ICONST_0));
        list.add(new InsnNode(IRETURN));
        list.add(label);

        method.instructions.insertBefore(node, list);
    }
}
