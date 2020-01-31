package dev.fiki.forgehax.asm.utils.transforming;

import cpw.mods.modlauncher.api.ITransformer;
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
public abstract class ClassTransformer implements ITransformer<ClassNode>, TypesMc, Opcodes, ASMHelper.MagicOpcodes {
  
  private final ASMClass transformingClass;
  private final List<MethodTransformer> methodTransformers = new ArrayList<>();
  
  public ClassTransformer(ASMClass clazz) {
    this.transformingClass = clazz;
    for (Class<?> c : getClass().getDeclaredClasses()) {
      try {
        if (c.isAnnotationPresent(RegisterMethodTransformer.class)
          && MethodTransformer.class.isAssignableFrom(c)) {
          Constructor<?> constructor;
          try {
            constructor = c.getDeclaredConstructor(getClass());
            constructor.setAccessible(true);
            MethodTransformer t = (MethodTransformer) constructor.newInstance(this);
            registerMethodPatch(t);
          } catch (NoSuchMethodException e) {
            constructor = c.getDeclaredConstructor();
            constructor.setAccessible(true);
            MethodTransformer t = (MethodTransformer) constructor.newInstance();
            registerMethodPatch(t);
          }
        }
      } catch (Throwable e) {
        getLogger().error("Exception thrown while generating method transformer list in {}",
            getTransformingClassName());
        getLogger().error(e, e);
      }
    }
  }
  
  public void registerMethodPatch(MethodTransformer transformer) {
    methodTransformers.add(transformer);
  }
  
  public String getTransformingClassName() {
    return transformingClass.getClassName();
  }

  @Nonnull
  @Override
  public final ClassNode transform(ClassNode node, ITransformerVotingContext ctx) {
    for (final MethodNode methodNode : node.methods) {
      Iterator<MethodTransformer> it = methodTransformers.listIterator();
      while (it.hasNext()) {
        MethodTransformer transformer = it.next();
        ASMMethod target = transformer.getMethod();
        // find a method that has a matching name and descriptor
        // check both mcp and srg mappings. i don't believe obfuscated mappings are of any worry anymore
        if ((methodNode.name.equals(target.getMcp()) && methodNode.desc.equals(target.getMcpDescriptor()))
            || (methodNode.name.equals(target.getSrg()) && methodNode.desc.equals(target.getSrgDescriptor()))) {
          // matching method has been found
          // loop through all the method transformer tasks and invoke them
          for(MethodTransformer.TaskElement task : transformer.getTasks()) {
            try {
              task.getMethod().invoke(task, methodNode);
            } catch (Throwable t) {
              // catch errors
              getLogger().error("Failed to transform task \"{}\" in method {}::{}[{}]",
                  task.getDescription(),
                  getTransformingClassName(), target.getMcp(), target.getMcpDescriptor());
              getLogger().error(t, t);
            }
          }
          // remove method from list
          it.remove();
        }
      }
    }
    return node;
  }

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
