package dev.fiki.forgehax.main.util.reflection;

import dev.fiki.forgehax.main.util.reflection.fasttype.FastClass;

/**
 * Created on 5/27/2017 by fr1kin
 *
 * <p>Minecraft classes and other classes need to be separated so that if the non-minecraft class
 * is required before minecraft classes are loaded, they are not accidentally loaded too early
 */
public interface FastReflectionForge {

  // net.minecraftforge.fml.loading.ModJarURLHandler
  interface ModJarURLHandler {
    interface Classes {
      FastClass<?> ModJarURLConnection =
          FastClass.builder()
              .className("net.minecraftforge.fml.loading.ModJarURLHandler$ModJarURLConnection")
              .build();
    }
  }

  // cpw.mods.modlauncher.Launcher
  interface Launcher {
    interface Methods {

    }
  }
  
  interface Fields {

  }
}
