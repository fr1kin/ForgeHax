package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.transformer.RegisterTransformer;
import com.matt.forgehax.asm.transformer.Transformer;
import com.matt.forgehax.asm.utils.ASMHelper;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import net.minecraft.world.World;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Set;

public class WorldPatch {

  //@RegisterTransformer // TODO: verify this
  public static class OnStoppedUsingItem implements Transformer<MethodNode> {
    @Nonnull
    @Override
    public Set<Target> targets() {
      return ASMHelper.getTargetSet(Methods.World_handleMaterialAcceleration);
    }

    @Nonnull
    @Override
    public MethodNode transform(MethodNode method, ITransformerVotingContext context) {
      AbstractInsnNode preNode =
          ASMHelper.findPattern(
              method.instructions.getFirst(),
              new int[] {
                  ALOAD,
                  INVOKEVIRTUAL,
                  ASTORE,
                  0x00,
                  0x00,
                  LDC,
                  DSTORE,
                  0x00,
                  0x00,
                  ALOAD,
                  DUP,
                  GETFIELD,
                  ALOAD,
                  GETFIELD,
                  LDC,
                  DMUL,
                  DADD,
                  PUTFIELD
              },
              "xxx??xx??xxxxxxxxx");
      AbstractInsnNode postNode =
          ASMHelper.findPattern(method.instructions.getFirst(), new int[] {ILOAD, IRETURN}, "xx");

      Objects.requireNonNull(preNode, "Find pattern failed for preNode");
      Objects.requireNonNull(postNode, "Find pattern failed for postNode");

      LabelNode endJump = new LabelNode();

      InsnList insnPre = new InsnList();
      insnPre.add(new VarInsnNode(ALOAD, 3));
      insnPre.add(new VarInsnNode(ALOAD, 11));
      insnPre.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onWaterMovement));
      insnPre.add(new JumpInsnNode(IFNE, endJump));

      method.instructions.insertBefore(preNode, insnPre);
      method.instructions.insertBefore(postNode, endJump);

      return method;
    }

  }


}
