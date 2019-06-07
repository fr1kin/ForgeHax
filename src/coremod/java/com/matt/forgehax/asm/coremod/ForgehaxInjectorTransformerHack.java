package com.matt.forgehax.asm.coremod;

import com.matt.forgehax.asm.coremod.transformer.Transformer;
import com.matt.forgehax.asm.coremod.utils.ASMHelper;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.TransformingClassLoader;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import org.objectweb.asm.tree.ClassNode;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.*;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class ForgehaxInjectorTransformerHack implements Transformer<ClassNode> {

  AtomicBoolean injected = new AtomicBoolean(false);

  @Nonnull
  @Override
  public ClassNode transform(ClassNode input, ITransformerVotingContext context) {

    if (!injected.getAndSet(true)) {
      getJarURL().ifPresent(url -> injectForgehaxUrl(url));
    }

    return input;
  }

  Optional<URL> getJarURL() {
    final String thisPath = this.getClass().getName().replace('.', '/') + ".class";
    final URL url = this.getClass().getClassLoader().getResource(thisPath);

    try {
      URLConnection urlConnection = url.openConnection();
      if (urlConnection instanceof JarURLConnection) {
        return Optional.of(((JarURLConnection)urlConnection).getJarFileURL());
      }

    } catch (IOException ex) {
      ex.printStackTrace();
    }

    return Optional.empty();
  }

  public static void injectForgehaxUrl(URL url) {
    try {
      Field f_classLoader = Launcher.class.getDeclaredField("classLoader");
      f_classLoader.setAccessible(true);
      Field f_delegatedClassLoader = TransformingClassLoader.class.getDeclaredField("delegatedClassLoader");
      f_delegatedClassLoader.setAccessible(true);
      Method m_addUrl = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
      m_addUrl.setAccessible(true);
      URLClassLoader delegate = (URLClassLoader)f_delegatedClassLoader.get(f_classLoader.get(Launcher.INSTANCE));

      m_addUrl.invoke(delegate, url);
    } catch (ReflectiveOperationException ex) {
      throw new RuntimeException("Failed to inject forgehax jar url ", ex);
    }
  }

  @Nonnull
  @Override
  public Set<Target> targets() {
    return ASMHelper.getTargetSet(
        Classes.Minecraft,
        Classes.Session, Classes.Session$Type // these classes seem to be loaded before Minecraft
    );
  }


}
