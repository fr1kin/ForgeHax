package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.utils.ASMHelper;
import com.matt.forgehax.asm.utils.asmtype.ASMMethod;
import com.matt.forgehax.asm.utils.transforming.ClassTransformer;
import com.matt.forgehax.asm.utils.transforming.Inject;
import com.matt.forgehax.asm.utils.transforming.MethodTransformer;
import com.matt.forgehax.asm.utils.transforming.RegisterMethodTransformer;
import org.objectweb.asm.tree.*;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

/**
 * Created by Babbaj on 4/9/2018.
 */
public class NettyDecompressionPatch extends ClassTransformer {
    public NettyDecompressionPatch() {
        super(Classes.NettyCompressionDecoder);
    }

    @RegisterMethodTransformer
    private class Decode extends MethodTransformer {
        @Override
        public ASMMethod getMethod() {
            return Methods.NettyCompressionDecoder_decode;
        }

        @Inject(description = "Add hook that removes NettyDecoder exception to bypass ban books")
        public void inject(MethodNode main) {
            AbstractInsnNode athrow = ASMHelper.findPattern(main.instructions.getFirst(), new int[] {
                    ATHROW,
            }, "x");
            AbstractInsnNode nextThrow = ASMHelper.findPattern(athrow.getNext(), new int[] {
                    ATHROW,
            }, "x");

            Objects.requireNonNull(athrow);
            Objects.requireNonNull(nextThrow);

            main.instructions.set(athrow, new InsnNode(POP));
            main.instructions.set(nextThrow, new InsnNode(POP));
        }

    }

}