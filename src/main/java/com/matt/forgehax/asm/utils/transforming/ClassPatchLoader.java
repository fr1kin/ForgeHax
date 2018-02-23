package com.matt.forgehax.asm.utils.transforming;

import com.matt.forgehax.util.classloader.AbstractClassLoader;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;

/**
 * Created on 2/13/2018 by fr1kin
 */
public class ClassPatchLoader extends AbstractClassLoader<ClassTransformer> {
    private static final ClassPatchLoader INSTANCE = new ClassPatchLoader();

    public static ClassPatchLoader getInstance() {
        return INSTANCE;
    }

    //
    //
    //

    @Nullable
    @Override
    public Class<ClassTransformer> getInheritedClass() {
        return ClassTransformer.class;
    }

    @Nullable
    @Override
    public Class<? extends Annotation> getAnnotationClass() {
        return RegisterClassPatch.class;
    }
}
