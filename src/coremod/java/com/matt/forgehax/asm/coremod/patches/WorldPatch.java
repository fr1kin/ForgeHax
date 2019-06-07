package com.matt.forgehax.asm.coremod.patches;

import com.matt.forgehax.asm.coremod.TypesHook;
import com.matt.forgehax.asm.coremod.transformer.RegisterTransformer;
import com.matt.forgehax.asm.coremod.transformer.Transformer;
import com.matt.forgehax.asm.coremod.utils.ASMHelper;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Set;

public class WorldPatch {

  //@RegisterTransformer // TODO: verify this
  public static class OnStoppedUsingItem implements Transformer<MethodNode> {
    @Nonnull
    @Override
    public Set<ITransformer.Target> targets() {
      return ASMHelper.getTargetSet(Methods.World_handleMaterialAcceleration);
    }

    @Nonnull
    @Override
    public MethodNode transform(MethodNode method, ITransformerVotingContext context) {
      AbstractInsnNode preNode =
          ASMHelper.findPattern(
              method.instructions.getFirst(),
              new int[] {
                  Opcodes.ALOAD,
                  Opcodes.INVOKEVIRTUAL,
                  Opcodes.ASTORE,
                  0x00,
                  0x00,
                  Opcodes.LDC,
                  Opcodes.DSTORE,
                  0x00,
                  0x00,
                  Opcodes.ALOAD,
                  Opcodes.DUP,
                  Opcodes.GETFIELD,
                  Opcodes.ALOAD,
                  Opcodes.GETFIELD,
                  Opcodes.LDC,
                  Opcodes.DMUL,
                  Opcodes.DADD,
                  Opcodes.PUTFIELD
              },
              "xxx??xx??xxxxxxxxx");
      AbstractInsnNode postNode =
          ASMHelper.findPattern(method.instructions.getFirst(), new int[] {Opcodes.ILOAD, Opcodes.IRETURN}, "xx");

      Objects.requireNonNull(preNode, "Find pattern failed for preNode");
      Objects.requireNonNull(postNode, "Find pattern failed for postNode");

      LabelNode endJump = new LabelNode();

      InsnList insnPre = new InsnList();
      insnPre.add(new VarInsnNode(Opcodes.ALOAD, 3));
      insnPre.add(new VarInsnNode(Opcodes.ALOAD, 11));
      insnPre.add(ASMHelper.call(Opcodes.INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onWaterMovement));
      insnPre.add(new JumpInsnNode(Opcodes.IFNE, endJump));

      method.instructions.insertBefore(preNode, insnPre);
      method.instructions.insertBefore(postNode, endJump);

      return method;
    }

  }


}
