package com.matt.forgehax.util.mod.loader;

import com.google.common.collect.Lists;
import com.matt.forgehax.Globals;
import com.matt.forgehax.Helper;
import com.matt.forgehax.util.ClassLoaderHelper;
import com.matt.forgehax.util.mod.BaseMod;
import net.minecraft.launchwrapper.Launch;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.matt.forgehax.Helper.getLog;

/**
 * Created on 5/16/2017 by fr1kin
 */
public class ForgeHaxModLoader implements Globals {

    @SuppressWarnings("unchecked")
    public static Collection<Class<? extends BaseMod>> getClassesInPackage(String pack) {
        try {
            return ClassLoaderHelper.getClassesForPackage(getFMLClassLoader(), pack).stream()
                    .filter(ForgeHaxModLoader::isClassValid)
                    .map(c -> (Class<? extends BaseMod>)c)
                    .collect(Collectors.toList());
        } catch (Throwable t) {
            Helper.handleThrowable(t);
        }
        return Collections.emptyList();
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
