package com.matt.forgehax.util.command.v2.flag;

import com.google.common.collect.Maps;
import com.matt.forgehax.util.serialization.ISerializableJson;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Map;

/**
 * Created on 12/25/2017 by fr1kin
 */
public interface ICommandFlagV2 extends ISerializableJson {
    class Registry {
        private static final Map<Class<? extends Enum<?>>, EnumSet<?>> REGISTRY = Maps.newConcurrentMap();

        public static <T extends Enum<T>> void register(Class<T> enumClass) {
            if(!ICommandFlagV2.class.isAssignableFrom(enumClass)) throw new IllegalArgumentException("Flag Enum must extend " + ICommandFlagV2.class.getSimpleName());
            REGISTRY.put(enumClass, EnumSet.allOf(enumClass));
        }

        @SuppressWarnings("unchecked")
        @Nullable
        public static <T extends Enum<T>> EnumSet<T> get(Class<T> enumClass) {
            return (EnumSet<T>)REGISTRY.get(enumClass);
        }

        @SuppressWarnings("unchecked")
        @Nullable
        public static EnumSet<? extends ICommandFlagV2> get(final String enumClassName) {
            return REGISTRY.entrySet().stream()
                    .filter(e -> e.getKey().getName().equals(enumClassName))
                    .findFirst()
                    .map(Map.Entry::getValue)
                    .map(es -> (EnumSet<? extends ICommandFlagV2>)es)
                    .orElse(null);
        }

        @SuppressWarnings("unchecked")
        @Nullable
        public static Enum<? extends ICommandFlagV2> get(final String enumClassName, final String enumValueName) {
            EnumSet<? extends ICommandFlagV2> es = get(enumClassName);
            return es == null ? null : es.stream()
                    .filter(v -> v.name().equals(enumValueName))
                    .findFirst()
                    .orElse(null);
        }

        @SuppressWarnings("unchecked")
        @Nullable
        public static Enum<? extends ICommandFlagV2> getFromSerializedString(final String serializedEnumStr) {
            String split[] = serializedEnumStr.split("::");
            if(split.length != 2) return null;
            return get(split[0], split[1]);
        }
    }
}
