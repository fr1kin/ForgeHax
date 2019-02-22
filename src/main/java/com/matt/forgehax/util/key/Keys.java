package com.matt.forgehax.util.key;

import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Keys {
    private Keys() {}

    public static Map<String, Integer> GLFW_KEYS = Collections.unmodifiableMap(getGlfwKeys());

    public static int getKeyByName(String name) {
        return getOptionalKeyByName(name).orElse(GLFW.GLFW_KEY_UNKNOWN);
    }

    public static Optional<Integer> getOptionalKeyByName(String name) {
        return GLFW_KEYS.keySet().stream()
                .filter(k -> k.contains(name))
                .findFirst()
                .map(GLFW_KEYS::get);
    }

    private static Map<String, Integer> getGlfwKeys() {
        return Stream.of(GLFW.class.getDeclaredFields())
                .filter(f -> f.getType() == int.class)
                .filter(f -> f.getName().startsWith("GLFW_KEY_"))
                .collect(Collectors.toMap(
                        Field::getName,
                        Keys::getStaticInt
                ));
    }

    private static int getStaticInt(Field f) {
        try {
            return f.getInt(null);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
