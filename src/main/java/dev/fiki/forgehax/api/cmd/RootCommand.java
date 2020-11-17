package dev.fiki.forgehax.api.cmd;

import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

public final class RootCommand extends AbstractParentCommand {
  private Path configurationDirectory;

  public RootCommand() {
    super(null, "root", Collections.emptySet(), "Root most node", Collections.emptySet());
    onFullyConstructed();
  }

  @SneakyThrows
  public void setConfigurationDirectory(Path configurationDirectory) {
    Files.createDirectories(configurationDirectory);
    this.configurationDirectory = configurationDirectory;
  }

  @Override
  public Path getConfigDirectory() {
    return configurationDirectory;
  }
}
