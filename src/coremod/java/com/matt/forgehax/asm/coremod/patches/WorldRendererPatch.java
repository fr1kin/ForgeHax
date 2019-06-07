package com.matt.forgehax.asm.coremod.patches;

import com.matt.forgehax.asm.coremod.TypesHook;
import com.matt.forgehax.asm.events.DrawBlockBoundingBoxEvent;
import com.matt.forgehax.asm.coremod.transformer.RegisterTransformer;
import com.matt.forgehax.asm.coremod.transformer.Transformer;
import com.matt.forgehax.asm.coremod.utils.ASMHelper;
import com.matt.forgehax.asm.coremod.utils.AsmPattern;
import com.matt.forgehax.asm.coremod.utils.InsnPattern;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Set;

public class WorldRendererPatch {

    @RegisterTransformer
    public static class SetupTerrain implements Transformer<MethodNode> {
        @Nonnull
        @Override
        public Set<ITransformer.Target> targets() {
            return ASMHelper.getTargetSet(Methods.WorldRenderer_setupTerrain);
        }

        @Nonnull
        @Override
        public MethodNode transform(MethodNode main, ITransformerVotingContext context) {
            // inject at this.mc.renderChunksMany
            InsnPattern node = new AsmPattern.Builder(AsmPattern.CODE_ONLY)
                    .opcodes(Opcodes.ALOAD, Opcodes.GETFIELD)
                    .ASMType(Opcodes.GETFIELD, Fields.Minecraft_renderChunksMany)
                    .opcode(Opcodes.ISTORE)
                    .build().test(main);

            Objects.requireNonNull(node, "Find pattern failed for node");

            LabelNode storeLabel = new LabelNode();
            LabelNode falseLabel = new LabelNode();

            InsnList insnList = new InsnList();
            insnList.add(new JumpInsnNode(Opcodes.IFEQ, falseLabel));
            insnList.add(ASMHelper.call(Opcodes.INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_shouldDisableCaveCulling));
            insnList.add(new JumpInsnNode(Opcodes.IFNE, falseLabel));
            insnList.add(new InsnNode(Opcodes.ICONST_1));
            insnList.add(new JumpInsnNode(Opcodes.GOTO, storeLabel));
            insnList.add(falseLabel);
            insnList.add(new InsnNode(Opcodes.ICONST_0));
            insnList.add(storeLabel);
            // iload should be below here

            main.instructions.insertBefore(node.getLast(), insnList);
            return main;
        }
    }

    @RegisterTransformer
    public static class RenderBlockLayer implements Transformer<MethodNode> {
        @Nonnull
        @Override
        public Set<ITransformer.Target> targets() {
            return ASMHelper.getTargetSet(Methods.WorldRenderer_renderBlockLayer);
        }

        @Nonnull
        @Override
        public MethodNode transform(MethodNode main, ITransformerVotingContext context) {
            AbstractInsnNode preNode = new AsmPattern.Builder(AsmPattern.CODE_ONLY)
                .ASMType(Opcodes.INVOKESTATIC, Methods.RenderHelper_disableStandardItemLighting)
                .build()
                .test(main).getFirst();

            AbstractInsnNode postNode =
                ASMHelper.findPattern(
                    main.instructions.getFirst(),
                    new int[] {Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.GETFIELD, Opcodes.INVOKEVIRTUAL, 0x00, 0x00, Opcodes.ILOAD, Opcodes.IRETURN},
                    "xxxx??xx");

            Objects.requireNonNull(preNode, "Find pattern failed for preNode");
            Objects.requireNonNull(postNode, "Find pattern failed for postNode");

            LabelNode endJump = new LabelNode();

            InsnList insnPre = new InsnList();
            insnPre.add(new InsnNode(Opcodes.ICONST_0));
            insnPre.add(new VarInsnNode(Opcodes.ISTORE, 5));
            insnPre.add(new VarInsnNode(Opcodes.ALOAD, 1));
            insnPre.add(new VarInsnNode(Opcodes.DLOAD, 2));
            insnPre.add(ASMHelper.call(Opcodes.INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onPreRenderBlockLayer));
            insnPre.add(new JumpInsnNode(Opcodes.IFNE, endJump));

            InsnList insnPost = new InsnList();
            insnPost.add(new VarInsnNode(Opcodes.ALOAD, 1));
            insnPost.add(new VarInsnNode(Opcodes.DLOAD, 2));
            insnPost.add(ASMHelper.call(Opcodes.INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onPostRenderBlockLayer));
            insnPost.add(endJump);

            main.instructions.insertBefore(preNode, insnPre);
            main.instructions.insertBefore(postNode, insnPost);

            return main;
        }
    }

    @RegisterTransformer
    public static class DrawBoundingBox implements Transformer<MethodNode> {
        @Nonnull
        @Override
        public Set<ITransformer.Target> targets() {
            return ASMHelper.getTargetSet(Methods.WorldRenderer_drawSelectionBox);
        }

        @Nonnull
        @Override
        public MethodNode transform(MethodNode main, ITransformerVotingContext context) {
            InsnPattern node = new AsmPattern.Builder(AsmPattern.CODE_ONLY)
                .opcodes(Opcodes.FCONST_0, Opcodes.FCONST_0, Opcodes.FCONST_0) // rgb
                .opcode(Opcodes.LDC) // alpha
                .ASMType(Opcodes.INVOKESTATIC, Methods.WorldRenderer_drawShape)
                .build()
                .test(main);
            Objects.requireNonNull(node, "Failed to find drawShape");
            final MethodInsnNode invokeInsn = node.getLast();

            final int eventIndex = ASMHelper.addNewLocalVariable(main, "forgehax_event", Type.getDescriptor(DrawBlockBoundingBoxEvent.Pre.class));

            {
                InsnList alloc = new InsnList();
                alloc.add(new TypeInsnNode(Opcodes.NEW, Type.getInternalName(DrawBlockBoundingBoxEvent.Pre.class)));
                alloc.add(new InsnNode(Opcodes.DUP));
                main.instructions.insertBefore(node.getFirst(), alloc); // event object allocated above color args
            }


            final InsnList pre = new InsnList();
            pre.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, Type.getInternalName(DrawBlockBoundingBoxEvent.Pre.class), "<init>", "(FFFF)V"));
            pre.add(new VarInsnNode(Opcodes.ASTORE, eventIndex)); // colors have been yoinked
            pre.add(new VarInsnNode(Opcodes.ALOAD, eventIndex));
            pre.add(ASMHelper.call(Opcodes.INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_fireEvent_v));
            pre.add(getColor("red", eventIndex));
            pre.add(getColor("green", eventIndex));
            pre.add(getColor("blue", eventIndex));
            pre.add(getColor("alpha", eventIndex));

            final InsnList post = new InsnList();
            post.add(ASMHelper.call(Opcodes.INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onDrawBoundingBox_Post));

            main.instructions.insertBefore(invokeInsn, pre);
            main.instructions.insert(invokeInsn, post);

            return main;
        }

        private InsnList getColor(String colorField, int eventIndex) {
            InsnList out = new InsnList();
            out.add(new VarInsnNode(Opcodes.ALOAD, eventIndex));
            out.add(new FieldInsnNode(Opcodes.GETFIELD, Type.getInternalName(DrawBlockBoundingBoxEvent.Pre.class), colorField, "F"));
            return out;
        }

    }
}
