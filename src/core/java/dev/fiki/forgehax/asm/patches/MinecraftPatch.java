package dev.fiki.forgehax.asm.patches;

import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.TransformingClassLoader;
import dev.fiki.forgehax.asm.TypesHook;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.asm.utils.ASMPattern;
import dev.fiki.forgehax.asm.utils.EZ;
import dev.fiki.forgehax.asm.utils.transforming.ClassTransformer;
import dev.fiki.forgehax.asm.utils.transforming.MethodTransformer;
import dev.fiki.forgehax.asm.utils.transforming.RegisterTransformer;
import dev.fiki.forgehax.common.asmtype.ASMClass;
import dev.fiki.forgehax.common.asmtype.ASMMethod;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import static dev.fiki.forgehax.asm.ASMCommon.getLogger;
import static dev.fiki.forgehax.asm.TypesMc.Fields.Minecraft_leftClickCounter;

public class MinecraftPatch {


  @RegisterTransformer("ForgeHaxHooks::onLeftClickCounterSet")
  public static class RunTick extends MethodTransformer {

    private boolean isLeftClickField(AbstractInsnNode node, int opcode) {
      if (node instanceof FieldInsnNode && node.getOpcode() == opcode) {
        FieldInsnNode fld = (FieldInsnNode) node;
        return Minecraft_leftClickCounter.isNameEqual(fld.name);
      }
      return false;
    }

    private boolean isPutLeftClickField(AbstractInsnNode node) {
      return isLeftClickField(node, PUTFIELD);
    }

    @Override
    public ASMMethod getMethod() {
      return Methods.Minecraft_runTick;
    }

    @Override
    public void transform(MethodNode method) {
      // this.leftClickCounter = 10000;
      AbstractInsnNode node = ASMPattern.builder()
          .opcodes(SIPUSH)
          .custom(this::isPutLeftClickField)
          .find(method)
          .getFirst();

      InsnList list = new InsnList();
      list.add(new VarInsnNode(ALOAD, 0));
      list.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onLeftClickCounterSet));

      method.instructions.insert(node, list);
    }
  }

  @RegisterTransformer("ForgeHaxHooks::onSendClickBlockToController")
  public static class SendClickBlockToController extends MethodTransformer {

    @Override
    public ASMMethod getMethod() {
      return Methods.Minecraft_sendClickBlockToController;
    }

    @Override
    public void transform(MethodNode method) {
      InsnList list = new InsnList();
      list.add(new VarInsnNode(ALOAD, 0));
      list.add(new VarInsnNode(ILOAD, 1));
      list.add(
          ASMHelper.call(
              INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onSendClickBlockToController));
      list.add(new VarInsnNode(ISTORE, 1));

      method.instructions.insert(list);
    }
  }

  //@RegisterTransformer("ForgeHaxHooks::LongNoseTribe")
  public static class HaltAccountEqualsBnw extends ClassTransformer {
    @Override
    public ASMClass getTransformingClass() {
      return ASMClass.builder()
          .className("net/minecraft/client/main/Main")
          .build();
    }

    @Override
    public void transform(ClassNode node) {
      EZ.getOurJar().ifPresent(this::injectUrl);
    }

    public void injectUrl(URL url) {
      try {
        Field classLoaderField = Launcher.class.getDeclaredField("classLoader");
        classLoaderField.setAccessible(true);

        Field delegatedClassLoaderField = TransformingClassLoader.class.getDeclaredField("delegatedClassLoader");
        delegatedClassLoaderField.setAccessible(true);

        Method addUrlMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        addUrlMethod.setAccessible(true);

        Launcher launcher = Launcher.INSTANCE;
        ClassLoader classLoader = (ClassLoader) classLoaderField.get(launcher);

        URLClassLoader delegate = (URLClassLoader) delegatedClassLoaderField.get(classLoader);
        addUrlMethod.invoke(delegate, url);
      } catch (ReflectiveOperationException ex) {
        getLogger().error("Failed to load ForgeHax", ex);
      }
    }
  }
}
