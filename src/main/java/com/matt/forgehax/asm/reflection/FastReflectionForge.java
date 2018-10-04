package com.matt.forgehax.asm.reflection;

import com.google.common.collect.BiMap;
import com.matt.forgehax.asm.utils.fasttype.FastField;
import com.matt.forgehax.asm.utils.fasttype.FastTypeBuilder;
import java.util.Map;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;

/**
 * Created on 5/27/2017 by fr1kin
 *
 * <p>Minecraft classes and other classes need to be separated so that if the non-minecraft class is
 * required before minecraft classes are loaded, they are not accidentally loaded too early
 */
public interface FastReflectionForge {
  interface Fields {
    /** FMLDeobfuscatingRemapper */
    FastField<BiMap<String, String>> FMLDeobfuscatingRemapper_classNameBiMap =
        FastTypeBuilder.create()
            .setInsideClass(FMLDeobfuscatingRemapper.class)
            .setName("classNameBiMap")
            .asField();

    FastField<Map<String, Map<String, String>>> FMLDeobfuscatingRemapper_rawFieldMaps =
        FastTypeBuilder.create()
            .setInsideClass(FMLDeobfuscatingRemapper.class)
            .setName("rawFieldMaps")
            .asField();
    FastField<Map<String, Map<String, String>>> FMLDeobfuscatingRemapper_rawMethodMaps =
        FastTypeBuilder.create()
            .setInsideClass(FMLDeobfuscatingRemapper.class)
            .setName("rawMethodMaps")
            .asField();
    FastField<Map<String, Map<String, String>>> FMLDeobfuscatingRemapper_fieldNameMaps =
        FastTypeBuilder.create()
            .setInsideClass(FMLDeobfuscatingRemapper.class)
            .setName("fieldNameMaps")
            .asField();
    FastField<Map<String, Map<String, String>>> FMLDeobfuscatingRemapper_methodNameMaps =
        FastTypeBuilder.create()
            .setInsideClass(FMLDeobfuscatingRemapper.class)
            .setName("methodNameMaps")
            .asField();
  }
}
