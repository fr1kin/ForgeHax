package com.matt.forgehax.asm.asmlib.patches;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.TypesMc;
import com.matt.forgehax.asm.utils.ASMHelper;
import com.matt.forgehax.asm.utils.InsnPattern;
import net.futureclient.asm.transformer.annotation.Inject;
import net.futureclient.asm.transformer.annotation.Transformer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import org.objectweb.asm.tree.*;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

@Transformer(RenderGlobal.class)
public class RenderGlobalPatch {

    @Inject(name = "loadRenderers",
    description = "At hook callback at end of method"
    )
    public void loadRenderers(MethodNode main) {
        InsnPattern node = ASMHelper._findPattern(main.instructions.getFirst(), new int[] {
                ALOAD, GETFIELD, IFNULL
        }, "xxx"); // if (this.world != null)
        Objects.requireNonNull(node, "Find pattern failed for node");
        final AbstractInsnNode injectionPoint = node.<JumpInsnNode>getLast().label;

        InsnList insnList = new InsnList();
        insnList.add(new VarInsnNode(ALOAD, 0));// push this
        insnList.add(ASMHelper.call(GETFIELD, TypesMc.Fields.RenderGlobal_viewFrustum));
        insnList.add(new VarInsnNode(ALOAD, 0)); // push this
        insnList.add(ASMHelper.call(GETFIELD, TypesMc.Fields.RenderGlobal_renderDispatcher));
        insnList.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onLoadRenderers));

        main.instructions.insert(injectionPoint, insnList);
    }


    @Inject(name = "renderBlockLayer",
            args = {BlockRenderLayer.class, double.class, int.class, Entity.class},
            ret = int.class,
    description = "Add hooks at the top and bottom of the method"
    )
    public void renderBlockLayer(MethodNode main) {
        AbstractInsnNode preNode = ASMHelper.findPattern(main.instructions.getFirst(), new int[] {
                INVOKESTATIC,
                0x00, 0x00,
                ALOAD, GETSTATIC, IF_ACMPNE,
                0x00, 0x00,
                ALOAD, GETFIELD, GETFIELD
        }, "x??xxx??xxx");
        AbstractInsnNode postNode = ASMHelper.findPattern(main.instructions.getFirst(), new int[] {
                ALOAD, GETFIELD, GETFIELD, INVOKEVIRTUAL,
                0x00, 0x00,
                ILOAD, IRETURN
        }, "xxxx??xx");

        Objects.requireNonNull(preNode, "Find pattern failed for preNode");
        Objects.requireNonNull(postNode, "Find pattern failed for postNode");

        LabelNode endJump = new LabelNode();

        InsnList insnPre = new InsnList();
        insnPre.add(new InsnNode(ICONST_0));
        insnPre.add(new VarInsnNode(ISTORE, 6));
        insnPre.add(new VarInsnNode(ALOAD, 1));
        insnPre.add(new VarInsnNode(DLOAD, 2));
        insnPre.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onPreRenderBlockLayer));
        insnPre.add(new JumpInsnNode(IFNE, endJump));

        InsnList insnPost = new InsnList();
        insnPost.add(new VarInsnNode(ALOAD, 1));
        insnPost.add(new VarInsnNode(DLOAD, 2));
        insnPost.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onPostRenderBlockLayer));
        insnPost.add(endJump);

        main.instructions.insertBefore(preNode, insnPre);
        main.instructions.insertBefore(postNode, insnPost);
    }



    @Inject(name = "setupTerrain", args = {Entity.class, double.class, ICamera.class, int.class, boolean.class},
    description = "Add hook at the top of the method"
    )
    public void setupTerrain(MethodNode main) {
        AbstractInsnNode node = ASMHelper.findPattern(main.instructions.getFirst(), new int[] {
                ALOAD, GETFIELD, GETFIELD, GETFIELD, ALOAD
        }, "xxxxx");

        Objects.requireNonNull(node, "Find pattern failed for node");

        InsnList insnPre = new InsnList();
        insnPre.add(new VarInsnNode(ALOAD, 1));
        insnPre.add(new VarInsnNode(ILOAD, 6));
        insnPre.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onSetupTerrain));
        insnPre.add(new VarInsnNode(ISTORE, 6));

        main.instructions.insertBefore(node, insnPre);
    }

    // same method as above
    @Inject(name = "setupTerrain", args = {Entity.class, double.class, ICamera.class, int.class, boolean.class},
            description = "Add or logic to this.mc.renderChunksMany flag"
    )
    public void injectAtFlag(MethodNode main) {
        // inject at this.mc.renderChunksMany
        AbstractInsnNode node = ASMHelper.findPattern(main.instructions.getFirst(), new int[] {
                ISTORE,
                0x00, 0x00,
                ALOAD, IFNULL,
                0x00, 0x00,
                ICONST_0, ISTORE,
                0x00, 0x00,
                NEW, DUP, ALOAD, ALOAD, ACONST_NULL, CHECKCAST, ICONST_0, ACONST_NULL, INVOKESPECIAL, ASTORE
        }, "x??xx??xx??xxxxxxxxxx");

        Objects.requireNonNull(node, "Find pattern failed for node");

        LabelNode storeLabel = new LabelNode();
        LabelNode falseLabel = new LabelNode();

        InsnList insnList = new InsnList();
        insnList.add(new JumpInsnNode(IFEQ, falseLabel));
        insnList.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_shouldDisableCaveCulling));
        insnList.add(new JumpInsnNode(IFNE, falseLabel));
        insnList.add(new InsnNode(ICONST_1));
        insnList.add(new JumpInsnNode(GOTO, storeLabel));
        insnList.add(falseLabel);
        insnList.add(new InsnNode(ICONST_0));
        insnList.add(storeLabel);
        // iload should be below here

        main.instructions.insertBefore(node, insnList);
    }
}
