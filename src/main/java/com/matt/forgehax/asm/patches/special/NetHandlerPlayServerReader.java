package com.matt.forgehax.asm.patches.special;

import com.matt.forgehax.asm.utils.asmtype.ASMMethod;
import com.matt.forgehax.asm.utils.transforming.ClassTransformer;
import com.matt.forgehax.asm.utils.transforming.Inject;
import com.matt.forgehax.asm.utils.transforming.MethodTransformer;
import com.matt.forgehax.asm.utils.transforming.RegisterMethodTransformer;
import com.matt.forgehax.mods.NoForgeMod;
import com.matt.forgehax.util.mod.loader.ModManager;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.objectweb.asm.Opcodes.*;

/**
 * Created by Babbaj on 4/7/2018.
 *
 * This class does not make any modifications but is used to read String literals
 */
public class NetHandlerPlayServerReader extends ClassTransformer {
    public NetHandlerPlayServerReader() {
        super(/*Classes.NetHandlerPlayServer*/null);
    }


    /*@RegisterMethodTransformer
    private class ReadStrings extends MethodTransformer {

        @Override
        public ASMMethod getMethod() {
            return Methods.NetHandlerPlayServer_processCustomPayload;
        }

        @Inject(description = "Read String literals from NetHandlerPlayServer")
        public void inject(MethodNode main) {
            AbstractInsnNode start = main.instructions.getFirst();
            final Pattern pattern = Pattern.compile("MC\\|.+");
            List<String> vanillaChannels = new ArrayList<>();

            AbstractInsnNode iter = start;
            while ((iter = iter.getNext()) != null) {
                if (iter instanceof LdcInsnNode) {
                    LdcInsnNode ldc = (LdcInsnNode) iter;
                    if (ldc.cst instanceof String && pattern.matcher((String)ldc.cst).matches())
                    {
                        vanillaChannels.add((String)ldc.cst);
                    }
                }
            }
            vanillaChannels.forEach(System.out::println);


            ModManager.getInstance().get(NoForgeMod.class).ifPresent(mod -> {
                mod.VANILLA_CHANNELS.addAll(vanillaChannels);
            });
        }
    }*/
}