package com.matt.forgehax.asm.coremod.patches;

import com.matt.forgehax.asm.coremod.transformer.RegisterTransformer;
import com.matt.forgehax.asm.coremod.transformer.Transformer;
import com.matt.forgehax.asm.coremod.utils.ASMHelper;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.TransformingClassLoader;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;

public class ForgehaxURLInjector {

  static final String INJECT_METHOD = "injectForgehaxUrl";

  @RegisterTransformer
  public static class Run implements Transformer<MethodNode> {
    @Nonnull
    @Override
    public Set<Target> targets() {
      return ASMHelper.getTargetSet(Methods.Minecraft_run);
    }

    @Nonnull
    @Override
    public MethodNode transform(MethodNode method, ITransformerVotingContext context) {
      AbstractInsnNode node = method.instructions.getFirst();
      InsnList list = new InsnList();
      list.add(new MethodInsnNode(INVOKESTATIC, Classes.Minecraft.getInternalName(), INJECT_METHOD, "()V", false));

      method.instructions.insert(node, list);
      return method;
    }


  }


  @RegisterTransformer
  public static class MethodInjector implements Transformer<ClassNode> {
    @Nonnull
    @Override
    public Set<Target> targets() {
      return ASMHelper.getTargetSet(Classes.Minecraft);
    }

    @Nonnull
    @Override
    public ClassNode transform(ClassNode clazz, ITransformerVotingContext context) {
      addForgehaxUrlInjector(clazz, "file:///C:\\Users\\babbaj\\AppData\\Roaming\\.minecraft\\mods\\forgehax-1.13.2-2.9.0.jar"); // temp url

      return clazz;
    }

    private void addForgehaxUrlInjector(ClassVisitor cw, String url) {
      MethodVisitor mv = mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "injectForgehaxUrl", "()V", null, null);
      mv.visitCode();
      Label l0 = new Label();
      Label l1 = new Label();
      Label l2 = new Label();
      mv.visitTryCatchBlock(l0, l1, l2, "java/lang/ReflectiveOperationException");
      mv.visitTryCatchBlock(l0, l1, l2, "java/net/MalformedURLException");
      mv.visitLabel(l0);
      mv.visitLineNumber(71, l0);
      mv.visitLdcInsn(Type.getType("Lcpw/mods/modlauncher/Launcher;"));
      mv.visitLdcInsn("classLoader");
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getDeclaredField", "(Ljava/lang/String;)Ljava/lang/reflect/Field;", false);
      mv.visitVarInsn(ASTORE, 0);
      Label l3 = new Label();
      mv.visitLabel(l3);
      mv.visitLineNumber(72, l3);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitInsn(ICONST_1);
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Field", "setAccessible", "(Z)V", false);
      Label l4 = new Label();
      mv.visitLabel(l4);
      mv.visitLineNumber(73, l4);
      mv.visitLdcInsn(Type.getType("Lcpw/mods/modlauncher/TransformingClassLoader;"));
      mv.visitLdcInsn("delegatedClassLoader");
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getDeclaredField", "(Ljava/lang/String;)Ljava/lang/reflect/Field;", false);
      mv.visitVarInsn(ASTORE, 1);
      Label l5 = new Label();
      mv.visitLabel(l5);
      mv.visitLineNumber(74, l5);
      mv.visitVarInsn(ALOAD, 1);
      mv.visitInsn(ICONST_1);
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Field", "setAccessible", "(Z)V", false);
      Label l6 = new Label();
      mv.visitLabel(l6);
      mv.visitLineNumber(75, l6);
      mv.visitLdcInsn(Type.getType("Ljava/net/URLClassLoader;"));
      mv.visitLdcInsn("addURL");
      mv.visitInsn(ICONST_1);
      mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
      mv.visitInsn(DUP);
      mv.visitInsn(ICONST_0);
      mv.visitLdcInsn(Type.getType("Ljava/net/URL;"));
      mv.visitInsn(AASTORE);
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getDeclaredMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", false);
      mv.visitVarInsn(ASTORE, 2);
      Label l7 = new Label();
      mv.visitLabel(l7);
      mv.visitLineNumber(76, l7);
      mv.visitVarInsn(ALOAD, 2);
      mv.visitInsn(ICONST_1);
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "setAccessible", "(Z)V", false);
      Label l8 = new Label();
      mv.visitLabel(l8);
      mv.visitLineNumber(77, l8);
      mv.visitVarInsn(ALOAD, 1);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETSTATIC, "cpw/mods/modlauncher/Launcher", "INSTANCE", "Lcpw/mods/modlauncher/Launcher;");
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Field", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Field", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
      mv.visitTypeInsn(CHECKCAST, "java/net/URLClassLoader");
      mv.visitVarInsn(ASTORE, 3);
      Label l9 = new Label();
      mv.visitLabel(l9);
      mv.visitLineNumber(79, l9);
      mv.visitVarInsn(ALOAD, 2);
      mv.visitVarInsn(ALOAD, 3);
      mv.visitInsn(ICONST_1);
      mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
      mv.visitInsn(DUP);
      mv.visitInsn(ICONST_0);
      mv.visitTypeInsn(NEW, "java/net/URL");
      mv.visitInsn(DUP);
      mv.visitLdcInsn(url); // inline
      mv.visitMethodInsn(INVOKESPECIAL, "java/net/URL", "<init>", "(Ljava/lang/String;)V", false);
      mv.visitInsn(AASTORE);
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", false);
      mv.visitInsn(POP);
      mv.visitLabel(l1);
      mv.visitLineNumber(82, l1);
      Label l10 = new Label();
      mv.visitJumpInsn(GOTO, l10);
      mv.visitLabel(l2);
      mv.visitLineNumber(80, l2);
      mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[]{"java/lang/Exception"});
      mv.visitVarInsn(ASTORE, 0);
      Label l11 = new Label();
      mv.visitLabel(l11);
      mv.visitLineNumber(81, l11);
      mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
      mv.visitInsn(DUP);
      mv.visitLdcInsn("Failed to inject forgehax jar url ");
      mv.visitVarInsn(ALOAD, 0);
      mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;Ljava/lang/Throwable;)V", false);
      mv.visitInsn(ATHROW);
      mv.visitLabel(l10);
      mv.visitLineNumber(83, l10);
      mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
      mv.visitInsn(RETURN);
      mv.visitLocalVariable("f_classLoader", "Ljava/lang/reflect/Field;", null, l3, l1, 0);
      mv.visitLocalVariable("f_delegatedClassLoader", "Ljava/lang/reflect/Field;", null, l5, l1, 1);
      mv.visitLocalVariable("m_addUrl", "Ljava/lang/reflect/Method;", null, l7, l1, 2);
      mv.visitLocalVariable("delegate", "Ljava/net/URLClassLoader;", null, l9, l1, 3);
      mv.visitLocalVariable("ex", "Ljava/lang/Exception;", null, l11, l10, 0);
      mv.visitMaxs(8, 4);
      mv.visitEnd();
    }



    /*public static void injectForgehaxUrl() {
      try {
        Field f_classLoader = Launcher.class.getDeclaredField("classLoader");
        f_classLoader.setAccessible(true);
        Field f_delegatedClassLoader = TransformingClassLoader.class.getDeclaredField("delegatedClassLoader");
        f_delegatedClassLoader.setAccessible(true);
        Method m_addUrl = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        m_addUrl.setAccessible(true);
        URLClassLoader delegate = (URLClassLoader)f_delegatedClassLoader.get(f_classLoader.get(Launcher.INSTANCE));

        m_addUrl.invoke(delegate, new URL("forgehax_url"));
      } catch (ReflectiveOperationException | MalformedURLException ex) {
        throw new RuntimeException("Failed to inject forgehax jar url ", ex);
      }
    }*/


  }
}
