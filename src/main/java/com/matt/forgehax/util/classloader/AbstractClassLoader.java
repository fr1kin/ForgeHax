package com.matt.forgehax.util.classloader;

import net.minecraft.launchwrapper.Launch;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created on 2/13/2018 by fr1kin
 */
public abstract class AbstractClassLoader<E> {
    protected AbstractClassLoader() {}

    /**
     * The class that must be extended
     * @return class
     */
    @Nullable
    public abstract Class<E> getInheritedClass();

    /**
     * The optional annotation class that must be on top of every class
     * @return null if no annotation is required
     */
    @Nullable
    public abstract Class<? extends Annotation> getAnnotationClass();

    /**
     * Gets all the classes in the package that extend 'extendsClass' and are annotated with 'annotationClass'
     * @param packageDir package directory to scan
     * @return collection of classes that match the required conditions
     * @throws IOException if package has trouble being read
     */
    @SuppressWarnings("unchecked")
    public Collection<Class<? extends E>> getClassesInPackage(String packageDir) throws IOException {
        return ClassLoaderHelper.getLoadedClassesInPackage(getFMLClassLoader(), packageDir).stream()
                .filter(this::checkAnnotation)
                .filter(this::checkInheritedClass)
                .map(this::wildCast)
                .filter(this::valid)
                .collect(Collectors.toList());
    }

    /**
     * Initializes all the classes from ::create and returns a list of non-null instances created from the provided classes
     * @param classes
     * @return
     */
    public Collection<? extends E> getClassInstances(Collection<Class<? extends E>> classes) {
        return classes.stream()
                .map(this::create)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    public Collection<? extends E> getClassInstances(String packageDir) throws IOException {
        return getClassInstances(getClassesInPackage(packageDir));
    }

    public abstract boolean valid(Class<? extends E> clazz);
    public abstract E create(Class<? extends E> clazz);

    @SuppressWarnings("unchecked")
    private Class<? extends E> wildCast(Class<?> clazz) {
        return (Class<? extends E>)clazz;
    }

    private boolean checkAnnotation(Class<?> clazz) {
        return getAnnotationClass() == null || clazz.isAnnotationPresent(getAnnotationClass());
    }

    private boolean checkInheritedClass(Class<?> clazz) {
        return getInheritedClass() == null || getInheritedClass().isAssignableFrom(clazz);
    }

    //
    //
    //

    public static ClassLoader getFMLClassLoader() {
        return Launch.classLoader;
    }
}
