package com.matt.forgehax.util.command.v2.flag;

import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Map;

public class CmdFlags {
    private static final Map<Class<? extends Enum<?>>, EnumSet<?>> REGISTRY = Maps.newConcurrentMap();

    public static <T extends Enum<T>> void register(Class<T> enumClass) {
        if(!ICmdFlag.class.isAssignableFrom(enumClass)) throw new IllegalArgumentException("Flag Enum must extend " + ICmdFlag.class.getSimpleName());
        REGISTRY.put(enumClass, EnumSet.allOf(enumClass));
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T extends Enum<T>> EnumSet<T> get(Class<T> enumClass) {
        return (EnumSet<T>)REGISTRY.get(enumClass);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static EnumSet<? extends ICmdFlag> get(final String enumClassName) {
        return (EnumSet<? extends ICmdFlag>)REGISTRY.entrySet().stream()
                .filter(e -> e.getKey().getName().equals(enumClassName))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(null);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static Enum<? extends ICmdFlag> get(final String enumClassName, final String enumValueName) {
        EnumSet<? extends ICmdFlag> es = get(enumClassName);
        return es == null ? null : es.stream()
                .filter(v -> v.name().equals(enumValueName))
                .findFirst()
                .orElse(null);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static Enum<? extends ICmdFlag> fromString(final String serializedEnumStr) {
        String split[] = serializedEnumStr.split("::");
        if(split.length != 2) return null;
        return get(split[0], split[1]);
    }

    public static String toString(Enum<? extends ICmdFlag> flag) {
        return flag.getDeclaringClass().getName() + "::" + flag.name();
    }
}
