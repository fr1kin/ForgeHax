package dev.fiki.forgehax.asm.utils;

import net.minecraftforge.fml.loading.moddiscovery.AbstractJarFileLocator;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.forgespi.locating.IModFile;

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
          final ModFile modFile = new ModFile(p, this);
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
