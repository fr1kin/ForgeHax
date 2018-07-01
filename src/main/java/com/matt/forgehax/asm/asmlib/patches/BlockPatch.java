package com.matt.forgehax.asm.asmlib.patches;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.utils.ASMHelper;
import net.futureclient.asm.transformer.AsmMethod;
import net.futureclient.asm.transformer.annotation.Inject;
import net.futureclient.asm.transformer.annotation.Transformer;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.objectweb.asm.tree.*;

import java.util.List;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

@Transformer(Block.class)
public class BlockPatch {

    @Inject(name = "addCollisionBoxToList",
            args = {IBlockState.class, World.class, BlockPos.class, AxisAlignedBB.class, List.class, Entity.class, boolean.class},
            description = "Redirects method to our hook and allows the vanilla code to be canceled from executing"
    )
    public void addCollisionBoxToList(AsmMethod main) {
        AbstractInsnNode node = main.method.instructions.getFirst();
        AbstractInsnNode end = ASMHelper.findPattern(main.method.instructions.getFirst(), new int[] {
                RETURN
        }, "x");

        Objects.requireNonNull(node, "Find pattern failed for node");
        Objects.requireNonNull(end, "Find pattern failed for end");

        LabelNode jumpPast = new LabelNode();

        InsnList insnList = new InsnList();
        insnList.add(new VarInsnNode(ALOAD, 0)); //block
        insnList.add(new VarInsnNode(ALOAD, 1)); //state
        insnList.add(new VarInsnNode(ALOAD, 2)); //world
        insnList.add(new VarInsnNode(ALOAD, 3)); //pos
        insnList.add(new VarInsnNode(ALOAD, 4)); //entityBox
        insnList.add(new VarInsnNode(ALOAD, 5)); //collidingBoxes
        insnList.add(new VarInsnNode(ALOAD, 6)); //entityIn
        insnList.add(new VarInsnNode(ILOAD, 7)); //bool
        insnList.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onAddCollisionBoxToList));
        insnList.add(new JumpInsnNode(IFNE, jumpPast));

        main.method.instructions.insertBefore(end, jumpPast);
        main.method.instructions.insertBefore(node, insnList);
    }

    @Inject(name = "canRenderInLayer", args = {IBlockState.class, BlockRenderLayer.class}, ret = boolean.class,
            description = "Changes in layer code so that we can change it"
    )
    public void canRenderInLayer(MethodNode main) {
        AbstractInsnNode node = ASMHelper.findPattern(main.instructions.getFirst(), new int[]{INVOKEVIRTUAL}, "x");

        Objects.requireNonNull(node, "Find pattern failed for node");

        InsnList insnList = new InsnList();

        // starting after INVOKEVIRTUAL on Block.getBlockLayer()

        insnList.add(new VarInsnNode(ASTORE, 3)); // store the result from getBlockLayer()
        insnList.add(new VarInsnNode(ALOAD, 0)); // push this
        insnList.add(new VarInsnNode(ALOAD, 1)); // push block state
        insnList.add(new VarInsnNode(ALOAD, 3)); // push this.getBlockLayer() result
        insnList.add(new VarInsnNode(ALOAD, 2)); // push the block layer of the block we are comparing to
        insnList.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onRenderBlockInLayer));
        // now our result is on the stack

        main.instructions.insert(node, insnList);
    }
}
