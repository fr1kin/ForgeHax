package dev.fiki.forgehax.asm;

import dev.fiki.forgehax.api.log.ForgeHaxLog4J2Configuration;
import dev.fiki.forgehax.asm.utils.EZ;
import lombok.extern.log4j.Log4j2;
import net.minecraftforge.fml.loading.moddiscovery.AbstractJarFileLocator;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.fml.loading.moddiscovery.ModFileParser;
import net.minecraftforge.forgespi.locating.IModFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Log4j2
public class ForgeHaxModLocator extends AbstractJarFileLocator {
  public ForgeHaxModLocator() {
    ForgeHaxLog4J2Configuration.create();
    log.debug("Created ForgeHaxModLocator");
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
