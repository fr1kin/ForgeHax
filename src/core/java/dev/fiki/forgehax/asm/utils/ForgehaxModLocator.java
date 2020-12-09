package dev.fiki.forgehax.asm.utils;

import net.minecraftforge.fml.loading.moddiscovery.AbstractJarFileLocator;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.fml.loading.moddiscovery.ModFileParser;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.forgespi.locating.IModLocator;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ForgehaxModLocator extends AbstractJarFileLocator {

  public ForgehaxModLocator() {
    System.out.println("Created ForgehaxModLocator");
  }

  @Override
  public List<IModFile> scanMods() {
    return EZ.getJarPath()
        .map(p -> {
          ModFile modFile;
          try {
            // for newer versions of forge
            modFile = new ModFile(p, this, ModFileParser::modsTomlParser);
          } catch (Throwable t) {
            // for old versions of forge
            try {
              modFile = ModFile.class.getConstructor(Path.class, IModLocator.class).newInstance(p, this);
            } catch (Throwable tt) {
              throw new Error("Unable to build ModFile instance. No valid constructor found!", tt);
            }
          }
          this.modJars.compute(modFile, (mf, fs) -> this.createFileSystem(mf));
          return Collections.singletonList((IModFile) modFile);
        })
        .orElse(Collections.emptyList());
  }

  @Override
  public String name() {
    return "ForgehaxModLocator";
  }

  @Override
  public void initArguments(Map<String, ?> arguments) {
  }


}
