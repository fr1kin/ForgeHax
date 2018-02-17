package com.matt.forgehax.util.mod.loader;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.matt.forgehax.Globals;
import com.matt.forgehax.util.mod.BaseMod;
import joptsimple.internal.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static com.matt.forgehax.Helper.getLog;

/**
 * Created on 5/16/2017 by fr1kin
 */
public class ModManager implements Globals {
    private static final ModManager INSTANCE = new ModManager();

    public static ModManager getInstance() {
        return INSTANCE;
    }

    private final Map<String, BaseMod> mods = Maps.newTreeMap(String.CASE_INSENSITIVE_ORDER);

    private final Set<Class<? extends BaseMod>> foundClasses = Sets.newHashSet();

    public void registerMod(@Nonnull BaseMod mod) {
        if(foundClasses.contains(mod.getClass()))
            mods.put(mod.getModName(), mod);
        else
            LOGGER.warn(String.format("Not registering mod \"%s\" because its class is missing in foundClasses", mod.getModName()));
    }

    public void unregisterMod(@Nonnull BaseMod mod) {
        // mod.getClass() should be in foundClasses
        if(mods.remove(mod.getModName()) != null) mod.unload();
    }

    public void unregisterAll() {
        forEach(this::unregisterMod);
    }

    public void refreshMods() {
        forEach(mod -> {
            mod.unload();
            mod.load();
        });
    }

    public void addClass(Class<?> clazz) {
        try {
            foundClasses.add((Class<? extends BaseMod>) clazz);
        } catch (Throwable t) {
            LOGGER.info("Attempted to register invalid class \"" + clazz.getName() + "\": " + t.getMessage());
        }
    }

    public void addClassesInPackage(String pkg) {
        LOGGER.info("Search for mods inside \"" + pkg + "\"");
        try {
            foundClasses.addAll(ForgeHaxModLoader.getInstance().getClassesInPackage(pkg));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getPluginClasses() {
        PluginLoader.getJars().forEach(jar -> {
            getLog().info(String.format("Found jar \"%s\"", jar.getName()));
            PluginLoader.getAllClasses(jar, Strings.EMPTY).stream()
                    .filter(clazz -> clazz.isAnnotationPresent(RegisterMod.class))
                    .filter(BaseMod.class::isAssignableFrom)
                    .map(clazz -> (Class<? extends BaseMod>)clazz)
                    .filter(ForgeHaxModLoader.getInstance()::valid)
                    .forEach(this::addClass);
        });
    }

    public void loadClasses() {
        ForgeHaxModLoader.getInstance().getClassInstances(foundClasses).forEach(this::registerMod);
    }

    public void reloadClasses() {
        LOGGER.info("Reloading mods");
        unregisterAll();
        loadClasses();
        forEach(BaseMod::load);
    }

    public void forEach(final Consumer<BaseMod> consumer) {
        mods.forEach((k, v) -> consumer.accept(v));
    }

    @Nullable
    public BaseMod getMod(String mod) {
        return mods.get(mod);
    }

    public Collection<Class<? extends BaseMod>> getLoadedClasses() {
        return Collections.unmodifiableCollection(foundClasses);
    }

    public Collection<BaseMod> getMods() {
        return Collections.unmodifiableCollection(mods.values());
    }
}
