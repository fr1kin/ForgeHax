package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.helper.AsmMethod;
import com.matt.forgehax.asm.helper.ClassTransformer;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

public class RenderGlobalPatch extends ClassTransformer {
    public final AsmMethod RENDER_BLOCK_LAYER = new AsmMethod()
            .setName("renderBlockLayer")
            .setObfuscatedName("a")
            .setArgumentTypes(NAMES.BLOCK_RENDER_LAYER, double.class, int.class, NAMES.ENTITY)
            .setReturnType(int.class)
            .setHooks(NAMES.ON_PRERENDER_BLOCKLAYER, NAMES.ON_POSTRENDER_BLOCKLAYER);

    public final AsmMethod SETUP_TERRAIN = new AsmMethod()
            .setName("setupTerrain")
            .setObfuscatedName("a")
            .setArgumentTypes(NAMES.ENTITY, double.class, NAMES.ICAMERA, int.class, boolean.class)
            .setReturnType(void.class)
            .setHooks(NAMES.ON_SETUP_TERRAIN);

    public RenderGlobalPatch() {
        registerHook(RENDER_BLOCK_LAYER);
        registerHook(SETUP_TERRAIN);
    }

    @Override
    public boolean onTransformMethod(MethodNode method) {
        if(method.name.equals(RENDER_BLOCK_LAYER.getRuntimeName()) &&
                method.desc.equals(RENDER_BLOCK_LAYER.getDescriptor())) {
            updatePatchedMethods(renderBlockLayerPatch(method));
            return true;
        } else if(method.name.equals(SETUP_TERRAIN.getRuntimeName()) &&
                method.desc.equals(SETUP_TERRAIN.getDescriptor())) {
            updatePatchedMethods(setupTerrainPatch(method));
            return true;
        } return false;
    }

    private final int[] renderBlockLayerPreSig = {
            INVOKESTATIC,
            0x00, 0x00,
            ALOAD, GETSTATIC, IF_ACMPNE,
            0x00, 0x00,
            ALOAD, GETFIELD, GETFIELD
    };

    private final int[] renderBlockLayerPostSig = {
            ALOAD, GETFIELD, GETFIELD, INVOKEVIRTUAL,
            0x00, 0x00,
            ILOAD, IRETURN
    };

    public boolean renderBlockLayerPatch(MethodNode node) {
        AbstractInsnNode preNode = findPattern("renderBlockLayer", "preNode",
                node.instructions.getFirst(), renderBlockLayerPreSig, "x??xxx??xxx");
        AbstractInsnNode postNode = findPattern("renderBlockLayer", "postNode",
                node.instructions.getFirst(), renderBlockLayerPostSig, "xxxx??xx");
        if(preNode != null && postNode != null) {
            LabelNode endJump = new LabelNode();

            InsnList insnPre = new InsnList();
            insnPre.add(new InsnNode(ICONST_0));
            insnPre.add(new VarInsnNode(ISTORE, 6));
            insnPre.add(new VarInsnNode(ALOAD, 1));
            insnPre.add(new VarInsnNode(DLOAD, 2));
            insnPre.add(new MethodInsnNode(INVOKESTATIC,
                    NAMES.ON_PRERENDER_BLOCKLAYER.getParentClass().getRuntimeName(),
                    NAMES.ON_PRERENDER_BLOCKLAYER.getRuntimeName(),
                    NAMES.ON_PRERENDER_BLOCKLAYER.getDescriptor(),
                    false
            ));
            insnPre.add(new JumpInsnNode(IFNE, endJump));

            InsnList insnPost = new InsnList();
            insnPost.add(new VarInsnNode(ALOAD, 1));
            insnPost.add(new VarInsnNode(DLOAD, 2));
            insnPost.add(new MethodInsnNode(INVOKESTATIC,
                    NAMES.ON_POSTRENDER_BLOCKLAYER.getParentClass().getRuntimeName(),
                    NAMES.ON_POSTRENDER_BLOCKLAYER.getRuntimeName(),
                    NAMES.ON_POSTRENDER_BLOCKLAYER.getDescriptor(),
                    false
            ));
            insnPost.add(endJump);

            node.instructions.insertBefore(preNode, insnPre);
            node.instructions.insertBefore(postNode, insnPost);
            return true;
        } else return false;
    }

    private final int[] setupTerrainSig = {
            ALOAD, GETFIELD, GETFIELD, GETFIELD, ALOAD
    };

    public boolean setupTerrainPatch(MethodNode method) {
        AbstractInsnNode node = findPattern("setupTerrain", "node",
                method.instructions.getFirst(), setupTerrainSig, "xxxxx");
        if(node != null) {
            InsnList insnPre = new InsnList();
            insnPre.add(new VarInsnNode(ALOAD, 1));
            insnPre.add(new VarInsnNode(ILOAD, 6));
            insnPre.add(new MethodInsnNode(INVOKESTATIC,
                    NAMES.ON_SETUP_TERRAIN.getParentClass().getRuntimeName(),
                    NAMES.ON_SETUP_TERRAIN.getRuntimeName(),
                    NAMES.ON_SETUP_TERRAIN.getDescriptor(),
                    false
            ));
            insnPre.add(new VarInsnNode(ISTORE, 6));

            method.instructions.insertBefore(node, insnPre);
            return true;
        } else return false;
    }
}
