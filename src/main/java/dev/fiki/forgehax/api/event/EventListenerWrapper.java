package dev.fiki.forgehax.api.event;

import com.google.common.collect.Maps;
import dev.fiki.forgehax.api.common.PriorityEnum;
import lombok.Getter;
import lombok.SneakyThrows;
import org.objectweb.asm.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

@Getter
public final class EventListenerWrapper implements EventListener {
  private static final Map<Method, Class<?>> METHOD_WRAPPERS = Maps.newConcurrentMap();

  private final Object declaringInstance;
  private final Method method;
  private final EventListener instance;
  private final int priority;
  private final int flags;

  @SneakyThrows
  EventListenerWrapper(Object declaringInstance, Method method) {
    if (!Modifier.isPublic(method.getModifiers())) {
      throw new IllegalArgumentException("Method \"" + method.getName() + "\" must be public!");
    } else if (method.getReturnType() != void.class) {
      throw new IllegalArgumentException("Method \"" + method.getName() + "\" must have a void return type!");
    } else if (method.getParameterCount() != 1) {
      throw new IllegalArgumentException("Method \"" + method.getName() + "\" must have exactly 1 argument!");
    } else if (!Event.class.isAssignableFrom(method.getParameterTypes()[0])) {
      throw new IllegalArgumentException("Method \"" + method.getName() + "\" argument must be assignable form of Event!");
    }

    this.declaringInstance = declaringInstance;
    this.method = method;
    this.instance = (EventListener) getOrCreateWrapperClass(method)
        .getConstructor(declaringInstance.getClass())
        .newInstance(declaringInstance);

    if (method.isAnnotationPresent(SubscribeListener.class)) {
      SubscribeListener an = method.getAnnotation(SubscribeListener.class);
      this.priority = an.priority().ordinal();
      this.flags = an.flags();
    } else {
      this.priority = PriorityEnum.DEFAULT.ordinal();
      this.flags = ListenerFlags.NONE;
    }
  }

  public Class<?> getEventType() {
    return method.getParameterTypes()[0];
  }

  @Override
  public final void run(Event event) {
    instance.run(event);
  }

  @Override
  public String toString() {
    return getDeclaringInstance().getClass().getCanonicalName() + "::" + getMethod().getName();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    EventListenerWrapper that = (EventListenerWrapper) o;

    return method.equals(that.method);
  }

  @Override
  public int hashCode() {
    return method.hashCode();
  }

  private static Class<?> getOrCreateWrapperClass(Method method) {
    return METHOD_WRAPPERS.computeIfAbsent(method, EventListenerWrapper::genWrapperClass);
  }

  @SneakyThrows
  private static Class<?> genWrapperClass(Method method) {
    final String methodName = method.getName();
    final Type declaring = Type.getType(method.getDeclaringClass());
    final Type wrapper = Type.getObjectType(declaring.getInternalName() + "$" + method.getName() + "Event");
    final Type eventType = Type.getType(method.getParameterTypes()[0]);

    ClassLoader cl = new ClassLoader(Thread.currentThread().getContextClassLoader()) {
      @Override
      protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] bytes = createWrapperClassBytes(wrapper, declaring, methodName, eventType);
        return defineClass(name, bytes, 0, bytes.length);
      }
    };

    return cl.loadClass(wrapper.getClassName());
  }

  private static byte[] createWrapperClassBytes(Type classType, Type targetType, String targetMethodName, Type eventType) {
    final ClassWriter cw = new ClassWriter(0);
    FieldVisitor fv;
    MethodVisitor mv;

    cw.visit(V1_8, ACC_PUBLIC | ACC_SUPER, classType.getInternalName(), null, "java/lang/Object", new String[]{Type.getType(EventListener.class).getInternalName()});

    cw.visitSource(".dynamic", null);

    {
      fv = cw.visitField(ACC_PRIVATE | ACC_FINAL, "instance", targetType.getDescriptor(), null, null);
      fv.visitEnd();
    }
    {
      mv = cw.visitMethod(ACC_PUBLIC, "<init>", Type.getMethodDescriptor(Type.VOID_TYPE, targetType), null, null);
      mv.visitCode();

      Label label0 = new Label();
      mv.visitLabel(label0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);

      Label label1 = new Label();
      mv.visitLabel(label1);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitVarInsn(ALOAD, 1);
      mv.visitFieldInsn(PUTFIELD, classType.getInternalName(), "instance", targetType.getDescriptor());

      Label label2 = new Label();
      mv.visitLabel(label2);
      mv.visitInsn(RETURN);

      Label label3 = new Label();
      mv.visitLabel(label3);
      mv.visitLocalVariable("this", classType.getDescriptor(), null, label0, label3, 0);
      mv.visitLocalVariable("instance", targetType.getDescriptor(), null, label0, label3, 1);
      mv.visitMaxs(2, 2);

      mv.visitEnd();
    }
    {
      mv = cw.visitMethod(ACC_PUBLIC, "run", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Event.class)), null, null);
      mv.visitCode();

      Label label0 = new Label();
      mv.visitLabel(label0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, classType.getInternalName(), "instance", targetType.getDescriptor());
      mv.visitVarInsn(ALOAD, 1);
      mv.visitTypeInsn(CHECKCAST, eventType.getInternalName());
      mv.visitMethodInsn(INVOKEVIRTUAL, targetType.getInternalName(), targetMethodName, Type.getMethodDescriptor(Type.VOID_TYPE, eventType), false);

      Label label1 = new Label();
      mv.visitLabel(label1);
      mv.visitInsn(RETURN);

      Label label2 = new Label();
      mv.visitLabel(label2);
      mv.visitLocalVariable("this", classType.getDescriptor(), null, label0, label2, 0);
      mv.visitLocalVariable("event", Type.getType(Event.class).getDescriptor(), null, label0, label2, 1);
      mv.visitMaxs(2, 2);

      mv.visitEnd();
    }
    cw.visitEnd();

    return cw.toByteArray();
  }
}
