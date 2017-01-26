package com.matt.forgehax.asm2.detours;

import com.fr1kin.asmhelper.types.ASMClass;
import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.Map;

/**
 * Created on 1/24/2017 by fr1kin
 */
public class ClassDetours {
    private static final Map<String, ClassDetour> CLASS_DETOUR_MAP = Maps.newHashMap();

    protected static void registerClass(ClassDetour classDetour) {
        CLASS_DETOUR_MAP.put(classDetour.getDetouredClass().getClassName(), classDetour);
    }

    public static boolean containsClassName(String className) {
        return CLASS_DETOUR_MAP.containsKey(className);
    }

    public static ClassDetour lookupClassName(String className) {
        // TODO: am passing transformed name, need to either find the mcp name or just use the realtime name
        return CLASS_DETOUR_MAP.get(className);
    }

    public static Map<String, ClassDetour> getClassDetours() {
        return Collections.unmodifiableMap(CLASS_DETOUR_MAP);
    }

    static {
        registerClass(new EntiyRendererDetours());
    }
}
