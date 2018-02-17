package com.matt.forgehax.util.mod.loader;

import com.matt.forgehax.util.classloader.AbstractClassLoader;
import com.matt.forgehax.util.mod.BaseMod;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;

/**
 * Created on 5/16/2017 by fr1kin
 */
public class ForgeHaxModLoader extends AbstractClassLoader<BaseMod> {
    private static final ForgeHaxModLoader INSTANCE = new ForgeHaxModLoader();

    public static ForgeHaxModLoader getInstance() {
        return INSTANCE;
    }

    @Override
    public Class<BaseMod> getInheritedClass() {
        return BaseMod.class;
    }

    @Override
    public Class<? extends Annotation> getAnnotationClass() {
        return RegisterMod.class;
    }

    @Override
    public boolean valid(Class<? extends BaseMod> clazz) {
        try {
            return clazz.getDeclaredConstructor() != null;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    @Override
    public BaseMod create(Class<? extends BaseMod> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            return null;
        }
    }
}
