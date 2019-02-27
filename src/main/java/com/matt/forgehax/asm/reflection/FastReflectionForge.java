package com.matt.forgehax.asm.reflection;

import com.google.common.collect.BiMap;
import com.matt.forgehax.asm.utils.fasttype.FastField;
import com.matt.forgehax.asm.utils.fasttype.FastTypeBuilder;
import java.util.Map;

/**
 * Created on 5/27/2017 by fr1kin
 *
 * <p>Minecraft classes and other classes need to be separated so that if the non-minecraft class is
 * required before minecraft classes are loaded, they are not accidentally loaded too early
 */
public interface FastReflectionForge {
  interface Fields {

  }
}
