package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.utils.ASMHelper;
import com.matt.forgehax.asm.utils.asmtype.ASMMethod;
import com.matt.forgehax.asm.utils.transforming.ClassTransformer;
import com.matt.forgehax.asm.utils.transforming.Inject;
import com.matt.forgehax.asm.utils.transforming.MethodTransformer;
import com.matt.forgehax.asm.utils.transforming.RegisterMethodTransformer;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Method;
import java.util.ListIterator;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

public class ThreadDownloaderPatch extends ClassTransformer {
    public ThreadDownloaderPatch() {
        super(Classes.ThreadDownloadingImageData);
    }

    @RegisterMethodTransformer
    private class LoadTexture extends MethodTransformer {
        @Override
        public ASMMethod getMethod() {
            return Methods.ThreadDownloader_loadTexture;
        }

        @Inject(description = "Remove useless code that breaks our SkinManager patch")
        public void inject(MethodNode main) {
            AbstractInsnNode node = ASMHelper.findPattern(main.instructions.getFirst(), new int[] {
                    ALOAD, ALOAD, INVOKESPECIAL
            }, "xxx");

            Objects.requireNonNull(node);

            for (int i = 0; i < 3; i++) {
                node = node.getNext();
                main.instructions.remove(node.getPrevious());
            }


            /*AbstractInsnNode post = node.getNext().getNext().getNext(); // node after invokespecial

            LabelNode jump = new LabelNode();

            InsnList list = new InsnList();
            list.add(new VarInsnNode(ALOAD, 0));
            list.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onSkinLoad));
            list.add(new JumpInsnNode(IFNE, jump));

            main.instructions.insertBefore(node, list);
            main.instructions.insertBefore(post, jump);*/

        }

    }

    @RegisterMethodTransformer
    private class LoadTextureFromServer extends MethodTransformer {
        @Override
        public ASMMethod getMethod() {
            return Methods.ThreadDownloader_loadTextureFromServer;
        }

        @Inject(description = "Add hook so we can handle texture downloading thread ourselves")
        public void inject(MethodNode main) {
            AbstractInsnNode pre = ASMHelper.findPattern(main.instructions.getFirst(), new int[] {
                    ALOAD, GETFIELD, INVOKEVIRTUAL, 0x00, 0x00, RETURN
            }, "xxx??x"); // Thread::start


            Objects.requireNonNull(pre, "Failed to find node");

            AbstractInsnNode post = pre;
            for (int i = 0; i < 3; i++) // set post to node past INVOKEVIRTUAL
                post = post.getNext();


            MethodInsnNode hookCall = ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onSkinDownload);
            main.instructions.remove(post.getPrevious()); // remove Thread::start call
            main.instructions.insertBefore(post, hookCall); // replace it with a call to our hook
        }
    }

    @RegisterMethodTransformer
    private class SetBufferedImage extends MethodTransformer {
        @Override
        public ASMMethod getMethod() {
            return Methods.ThreadDownloader_setBufferedImage;
        }

        @Inject(description = "Add event for when a skin is downloaded")
        public void inject(MethodNode main) {
            AbstractInsnNode node = ASMHelper.findPattern(main.instructions.getFirst(), new int[] {
                    ALOAD, ALOAD, PUTFIELD
            }, "xxx"); // this.bufferedImage = bufferedImageIn;

            Objects.requireNonNull(node, "Failed to find node");

            AbstractInsnNode post = node.getNext().getNext(); // set to putfield node

            InsnList list = new InsnList();
            list.add(new VarInsnNode(ALOAD, 0));
            list.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onSkinAvailable));

            main.instructions.insert(post, list);
        }

    }
}
