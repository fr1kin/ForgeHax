package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.utils.ASMHelper;
import com.matt.forgehax.asm.utils.asmtype.ASMMethod;
import com.matt.forgehax.asm.utils.transforming.ClassTransformer;
import com.matt.forgehax.asm.utils.transforming.Inject;
import com.matt.forgehax.asm.utils.transforming.MethodTransformer;
import com.matt.forgehax.asm.utils.transforming.RegisterMethodTransformer;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.item.EntityTNTPrimed;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

/**
 * Created by Babbaj on 2/17/2018
 */
public class NetHandlerPlayClientPatch extends ClassTransformer {
    public NetHandlerPlayClientPatch() {
        super(Classes.NetHandlerPlayClient);
    }

    @RegisterMethodTransformer
    private class HandlePlayerListItem extends MethodTransformer {
        @Override
        public ASMMethod getMethod() {
            return Methods.NetHandlerPlayClient_handlePlayerListItem;
        }

        @Inject(description = "Add hook to fire event after the NetworkPlayerInfo is created")
        public void inject(MethodNode main) {
            AbstractInsnNode node = ASMHelper.findPattern(main.instructions.getFirst(), new int[] {
                    ALOAD, ALOAD, INVOKEVIRTUAL, INVOKEVIRTUAL
            }, "xxxx"); // case ADD_PLAYER

            Objects.requireNonNull(node);

            AbstractInsnNode post = node.getNext().getNext().getNext(); // set node to last invokevirtual

            InsnList list = new InsnList();
            list.add(new VarInsnNode(ALOAD, 4)); // networkplayerinfo
            list.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgehaxHooks_onPlayerListAdd));

            main.instructions.insert(post, list);
        }
    }
}
