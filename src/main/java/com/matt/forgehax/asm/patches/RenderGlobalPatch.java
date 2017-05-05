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
        super("net/minecraft/client/renderer/RenderGlobal");
    }

    @RegisterPatch
    private class RenderBlockLayer extends MethodTransformer {
        @Override
        public AsmMethod getMethod() {
            return RENDER_BLOCK_LAYER;
        }

        @Inject
        public void inject(MethodNode main) {
            AbstractInsnNode preNode = AsmHelper.findPattern(main.instructions.getFirst(), new int[] {
                    INVOKESTATIC,
                    0x00, 0x00,
                    ALOAD, GETSTATIC, IF_ACMPNE,
                    0x00, 0x00,
                    ALOAD, GETFIELD, GETFIELD
            }, "x??xxx??xxx");
            AbstractInsnNode postNode = AsmHelper.findPattern(main.instructions.getFirst(), new int[] {
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

            main.instructions.insertBefore(preNode, insnPre);
            main.instructions.insertBefore(postNode, insnPost);
        }
    }

    @RegisterPatch
    private class SetupTerrain extends MethodTransformer {
        @Override
        public AsmMethod getMethod() {
            return SETUP_TERRAIN;
        }

        @Inject
        public void inject(MethodNode main) {
            AbstractInsnNode node = AsmHelper.findPattern(main.instructions.getFirst(), new int[] {
                    ALOAD, GETFIELD, GETFIELD, GETFIELD, ALOAD
            }, "xxxxx");

            Objects.requireNonNull(node, "Find pattern failed for node");

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

            main.instructions.insertBefore(node, insnPre);
        }
    }
}
