package com.matt.forgehax.util.mod.loader;

import com.google.common.collect.Lists;
import com.google.common.reflect.ClassPath;
import com.matt.forgehax.Globals;
import com.matt.forgehax.Helper;
import com.matt.forgehax.util.mod.BaseMod;
import net.minecraft.launchwrapper.Launch;
import org.reflections.Reflections;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.matt.forgehax.Helper.getLog;

/**
 * Created on 5/16/2017 by fr1kin
 */
public class ForgeHaxModLoader implements Globals {

    public static Collection<Class<? extends BaseMod>> getClassesInPackage(String pack) {
        try {
            ClassPath classPath = ClassPath.from(getFMLClassLoader());
            Reflections reflections = new Reflections(pack);
            reflections.getSubTypesOf(Object.class);
            return filterClassInfo(classPath.getTopLevelClasses(pack));
        } catch (IOException e) {
            Helper.handleThrowable(e);
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    protected static Collection<Class<? extends BaseMod>> filterClassInfo(Collection<ClassPath.ClassInfo> input) {
        final List<Class<? extends BaseMod>> classes = Lists.newArrayList();
        input.forEach(info -> {
            try {
                Class<?> clazz = info.load();
                if(isClassValid(clazz)) classes.add((Class<? extends BaseMod>)clazz);
            } catch (Exception e) {
                getLog().warn(String.format("[%s] '%s' is not a valid mod class: %s", e.getClass().getSimpleName(), info.getSimpleName(), e.getMessage()));
            }
        });
        return Collections.unmodifiableCollection(classes);
    }

    protected static boolean isClassValid(Class<?> clazz) {
        try {
            Objects.requireNonNull(clazz);
            if(!clazz.isAnnotationPresent(RegisterMod.class))
                throw new Exception("missing @RegisterMod annotation");
            if(!BaseMod.class.isAssignableFrom(clazz))
                throw new Exception("does not extend BaseMod class");
            if(clazz.getDeclaredConstructor() == null)
                throw new Exception("missing default constructor");
            return true;
        } catch (Throwable t) {
            getLog().warn(String.format("Invalid class \"%s\": %s", clazz.getName(), t.getMessage()));
            return false;
        }
    }

    public static Collection<BaseMod> loadClasses(Collection<Class<? extends BaseMod>> classes) {
        List<BaseMod> mods = Lists.newArrayList();
        classes.forEach(clazz -> {
            try {
                mods.add(clazz.getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                Helper.printStackTrace(e);
                getLog().warn(String.format("Failed to create a new instance of '%s': %s", clazz.getSimpleName(), e.getMessage()));
            }
        });
        return Collections.unmodifiableCollection(mods);
    }

    private static ClassLoader getFMLClassLoader() {
        return Launch.classLoader;
    }
}
