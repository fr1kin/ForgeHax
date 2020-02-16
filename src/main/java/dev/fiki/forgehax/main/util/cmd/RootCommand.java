package dev.fiki.forgehax.main.util.cmd;

import com.google.gson.*;
import dev.fiki.forgehax.main.util.cmd.argument.IArgument;
import dev.fiki.forgehax.main.util.cmd.argument.RawArgument;
import dev.fiki.forgehax.main.util.cmd.flag.EnumFlag;
import dev.fiki.forgehax.main.util.serialization.IJsonSerializable;
import joptsimple.internal.Strings;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static dev.fiki.forgehax.main.Common.getLogger;

public final class RootCommand extends AbstractParentCommand {
  private Path configDir;

  public RootCommand() {
    super(null, "root", Collections.emptySet(), "Root most node", Collections.emptySet());
    onFullyConstructed();
  }

  @SneakyThrows
  public void setConfigDir(Path configDir) {
    Files.createDirectories(configDir);
    this.configDir = configDir;
  }

  private Path getCommandFilePath(ICommand command, String saveAs) {
    return configDir.resolve(command.getName()
        + (Strings.isNullOrEmpty(saveAs) ? "" : ("." + saveAs))
        + ".json");
  }

  private Path getCommandFilePath(ICommand command) {
    return getCommandFilePath(command, null);
  }

  private Gson getGson() {
    return new GsonBuilder()
        .setPrettyPrinting()
        .create();
  }

  private JsonParser getJsonParser() {
    return new JsonParser();
  }

  private void serializeCommand(ICommand command, JsonObject head) {
    if (command instanceof IParentCommand) {
      // IParentCommand cannot extend IJsonSerializable
      JsonObject next = new JsonObject();

      for (ICommand child : ((IParentCommand) command).getChildren()) {
        serializeCommand(child, next);
      }

      if (next.size() > 0) {
        head.add(command.getName(), next);
      }
    } else if (command instanceof IJsonSerializable) {
      try {
        head.add(command.getName(), ((IJsonSerializable) command).serialize());
      } catch (Throwable t) {
        getLogger().warn("Could not serialize \"{}\"", command.getName());
        getLogger().warn(t, t);
      }
    }
  }

  private void deserializeCommand(ICommand command, JsonElement element) {
    if (command instanceof IParentCommand && element.isJsonObject()) {
      JsonObject head = element.getAsJsonObject();
      for (ICommand child : ((IParentCommand) command).getChildren()) {
        if (head.has(child.getName())) {
          JsonElement next = head.get(child.getName());
          deserializeCommand(child, next);
        }
      }
    } else if (command instanceof IJsonSerializable) {
      try {
        ((IJsonSerializable) command).deserialize(element);
      } catch (Throwable t) {
        getLogger().warn("Could not deserialize \"{}\"", command.getName());
        getLogger().warn(t, t);
      }
    }
  }

  public void serialize() {
    final Gson gson = getGson();

    for (ICommand command : getChildren()) {
      serialize(command, getCommandFilePath(command), gson);
    }
  }

  public void deserialize() {
    final JsonParser parser = new JsonParser();

    for (ICommand command : getChildren()) {
      deserialize(command, getCommandFilePath(command), parser);
    }
  }

  private void serialize(ICommand command, Path config, Gson gson) {
    JsonObject object = new JsonObject();

    serializeCommand(command, object);

    if (object.size() <= 0)
      return; // nothing to save

    String json = gson.toJson(object);

    try {
      Files.write(config, json.getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      getLogger().warn("Failed to write command config for \"{}\"", command.getName());
      getLogger().warn(e, e);
    }
  }

  public void serialize(ICommand command) {
    serialize(command, getCommandFilePath(command), getGson());
  }

  private void deserialize(ICommand command, Path config, JsonParser parser) {
    if (!Files.exists(config)) {
      return;
    }

    try {
      JsonElement e = parser.parse(new String(Files.readAllBytes(config), StandardCharsets.UTF_8));
      JsonObject head = e.getAsJsonObject();
      deserializeCommand(command, head.get(command.getName()));
    } catch (IOException e) {
      getLogger().warn("Failed to read json config \"{}\"", command.getName());
      getLogger().warn(e, e);
    }
  }

  public void deserialize(ICommand command) {
    deserialize(command, getCommandFilePath(command), getJsonParser());
  }
}
