package dev.fiki.forgehax.asm;

import dev.fiki.forgehax.asm.utils.EZ;
import net.minecraftforge.fml.loading.moddiscovery.AbstractJarFileLocator;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.fml.loading.moddiscovery.ModFileParser;
import net.minecraftforge.forgespi.locating.IModFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static dev.fiki.forgehax.asm.ASMCommon.getLogger;

public class ForgeHaxModLocator extends AbstractJarFileLocator {

  public ForgeHaxModLocator() {
    getLogger().debug("Created ForgeHaxModLocator");
  }

  @Override
  public List<IModFile> scanMods() {
    return EZ.getJarPath()
        .map(p -> {
          final ModFile modFile = new ModFile(p, this, ModFileParser::modsTomlParser);
          this.modJars.compute(modFile, (mf, fs) -> this.createFileSystem(mf));

          return Collections.singletonList((IModFile) modFile);
        })
        .orElse(Collections.emptyList());
  }

  @Override
  public String name() {
    return "ForgeHaxModLocator";
  }

  @Override
  public void initArguments(Map<String, ?> arguments) {
  }
}
