package com.matt.forgehax.asm.helper.transforming;

import com.google.common.collect.Lists;
import com.matt.forgehax.asm.ASMCommon;
import com.matt.forgehax.asm.Names;
import com.matt.forgehax.asm.helper.AsmClass;
import com.matt.forgehax.asm.helper.AsmStackLogger;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Objects;

import static com.matt.forgehax.asm.helper.AsmStackLogger.*;

public abstract class ClassTransformer implements ASMCommon {
    protected final Names NAMES = Names.INSTANCE;

    private final AsmClass transformingClass;
    private final List<MethodTransformer> methodTransformers = Lists.newArrayList();

    public ClassTransformer(AsmClass clazz) {
        this.transformingClass = clazz;
        for(Class c : getClass().getDeclaredClasses()) {
            try {
                if (c.isAnnotationPresent(RegisterMethodTransformer.class) && MethodTransformer.class.isAssignableFrom(c)) {
                    Constructor constructor = c.getDeclaredConstructor(getClass());
                    constructor.setAccessible(true);
                    MethodTransformer t = (MethodTransformer)constructor.newInstance(this);
                    registerMethodPatch(t);
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
                AsmStackLogger.printStackTrace(e);
            }
        }
    }

    public ClassTransformer(String className) {
        this(new AsmClass().setName(className));
    }

    public void registerMethodPatch(MethodTransformer transformer) {
        methodTransformers.add(transformer);
    }

    public AsmClass getTransformingClass() {
        return transformingClass;
    }

    public String getTransformingClassName() {
        return transformingClass.getName().replaceAll("/", ".");
    }

    public final void transform(final ClassNode node) {
        for(final MethodNode methodNode : node.methods) methodTransformers.stream()
                .filter(t -> Objects.equals(t.getMethod().getRuntimeName(), methodNode.name) && Objects.equals(t.getMethod().getDescriptor(), methodNode.desc))
                .forEach(t -> t.getTasks().forEach(task -> {
                    try {
                        task.getMethod().invoke(t, methodNode);
                        // if we have gotten this far the transformation should have been successful
                        StringBuilder builder = new StringBuilder();
                        builder.append("Successfully transformed the task \"");
                        builder.append(task.getDescription());
                        builder.append("\" for ");
                        builder.append(getTransformingClassName());
                        builder.append("::");
                        builder.append(t.getMethod().getName());
                        LOGGER.info(builder.toString());
                    } catch (Exception e) {
                        StringBuilder builder = new StringBuilder();
                        builder.append(e.getClass().getSimpleName()); // exception name
                        builder.append(" thrown from ");
                        builder.append(getTransformingClassName());
                        builder.append("::");
                        builder.append(t.getMethod().getName());
                        builder.append(" for the task with the description \"");
                        builder.append(task.getDescription());
                        builder.append("\": ");
                        builder.append(e.getMessage());
                        LOGGER.error(builder.toString());
                        printStackTrace(e);
                    }
                }));
    }
}
