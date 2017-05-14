package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.helper.AsmField;
import com.matt.forgehax.asm.helper.AsmHelper;
import com.matt.forgehax.asm.helper.AsmMethod;
import com.matt.forgehax.asm.helper.transforming.ClassTransformer;
import com.matt.forgehax.asm.helper.transforming.Inject;
import com.matt.forgehax.asm.helper.transforming.MethodTransformer;
import com.matt.forgehax.asm.helper.transforming.RegisterMethodTransformer;
import org.objectweb.asm.tree.*;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

public class RenderGlobalPatch extends ClassTransformer {
    public final AsmMethod LOAD_RENDERERS = new AsmMethod()
            .setName("loadRenderers")
            .setObfuscatedName("a")
            .setArgumentTypes()
            .setReturnType(void.class);

    public final AsmMethod RENDER_BLOCK_LAYER = new AsmMethod()
            .setName("renderBlockLayer")
            .setObfuscatedName("a")
            .setArgumentTypes(NAMES.BLOCK_RENDER_LAYER, double.class, int.class, NAMES.ENTITY)
            .setReturnType(int.class);

    public final AsmMethod SETUP_TERRAIN = new AsmMethod()
            .setName("setupTerrain")
            .setObfuscatedName("a")
            .setArgumentTypes(NAMES.ENTITY, double.class, NAMES.ICAMERA, int.class, boolean.class)
            .setReturnType(void.class)
            .setHooks(NAMES.ON_SETUP_TERRAIN);

    public RenderGlobalPatch() {
        super("net/minecraft/client/renderer/RenderGlobal");
    }

    @RegisterMethodTransformer
    private class LoadRenderers extends MethodTransformer {
        public final AsmField VIEW_FRUSTUM = new AsmField()
                .setName("viewFrustum")
                .setObfuscatedName("o")
                .setParentClass(NAMES.RENDER_GLOBAL)
                .setType(NAMES.VIEW_FRUSTUM);

        public final AsmField RENDER_DISPATCHER = new AsmField()
                .setName("renderDispatcher")
                .setObfuscatedName("N")
                .setParentClass(NAMES.RENDER_GLOBAL)
                .setType(NAMES.CHUNK_RENDER_DISPATCHER);

        @Override
        public AsmMethod getMethod() {
            return LOAD_RENDERERS;
        }

        @Inject
        public void inject(MethodNode main) {
            AbstractInsnNode node = AsmHelper.findPattern(main.instructions.getFirst(), new int[] {
                    PUTFIELD,
                    0x00, 0x00, 0x00,
                    RETURN
            }, "x???x");

            Objects.requireNonNull(node, "Find pattern failed for node");

            InsnList insnList = new InsnList();
            insnList.add(new VarInsnNode(ALOAD, 0));// push this
            insnList.add(new FieldInsnNode(GETFIELD,
                    VIEW_FRUSTUM.getParentClass().getRuntimeName(),
                    VIEW_FRUSTUM.getRuntimeName(),
                    VIEW_FRUSTUM.getTypeDescriptor()
            )); // load viewFrustum onto stack
            insnList.add(new VarInsnNode(ALOAD, 0)); // push this
            insnList.add(new FieldInsnNode(GETFIELD,
                    RENDER_DISPATCHER.getParentClass().getRuntimeName(),
                    RENDER_DISPATCHER.getRuntimeName(),
                    RENDER_DISPATCHER.getTypeDescriptor()
            )); // load renderDispatcher onto stack
            insnList.add(new MethodInsnNode(INVOKESTATIC,
                    NAMES.ON_LOAD_RENDERERS.getParentClass().getRuntimeName(),
                    NAMES.ON_LOAD_RENDERERS.getRuntimeName(),
                    NAMES.ON_LOAD_RENDERERS.getDescriptor(),
                    false
            ));

            main.instructions.insert(node, insnList);
        }
    }

    @RegisterMethodTransformer
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

    @RegisterMethodTransformer
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

        @Inject
        public void injectAtFlag(MethodNode main) {
            // inject at this.mc.renderChunksMany
            AbstractInsnNode node = AsmHelper.findPattern(main.instructions.getFirst(), new int[] {
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
            insnList.add(new MethodInsnNode(INVOKESTATIC,
                    NAMES.SHOULD_DISABLE_CULLING.getParentClass().getRuntimeName(),
                    NAMES.SHOULD_DISABLE_CULLING.getRuntimeName(),
                    NAMES.SHOULD_DISABLE_CULLING.getDescriptor(),
                    false
            ));
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
}
