package dev.fiki.forgehax.asm.utils.transforming;

import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import dev.fiki.forgehax.asm.TypesMc;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.common.asmtype.ASMClass;

import java.lang.reflect.Constructor;
import java.util.*;

import dev.fiki.forgehax.common.asmtype.ASMMethod;
import lombok.Getter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nonnull;

import static dev.fiki.forgehax.asm.ASMCommon.*;

@Getter
public abstract class ClassTransformer implements ITransformerProvider<ClassNode>, TypesMc, Opcodes, ASMHelper.MagicOpcodes {
  
  private final ASMClass transformingClass;

  public ClassTransformer(ASMClass clazz) {
    this.transformingClass = clazz;

  }

  public String getTransformingClassName() {
    return transformingClass.getClassName();
  }

  /*@Nonnull
  @Override
  public ClassNode transform(ClassNode node, ITransformerVotingContext ctx) {

    return node;
  }*/

  @Nonnull
  @Override
  public TransformerVoteResult castVote(ITransformerVotingContext context) {
    return TransformerVoteResult.YES;
  }

  @Nonnull
  @Override
  public Set<Target> targets() {
    return Collections.singleton(Target.targetClass(getTransformingClassName()));
  }
}
