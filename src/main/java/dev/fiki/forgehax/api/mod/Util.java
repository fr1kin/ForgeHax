package dev.fiki.forgehax.api.mod;

import dev.fiki.forgehax.api.cmd.AbstractCommand;
import lombok.SneakyThrows;

import java.lang.reflect.Field;

class Util {
  @SneakyThrows
  public static void setCommandName(AbstractCommand instance, String name) {
    final Field fName = AbstractCommand.class.getDeclaredField("name");
    fName.setAccessible(true);
    fName.set(instance, name);
  }

  @SneakyThrows
  public static void setCommandDescription(AbstractCommand instance, String description) {
    final Field fDesc = AbstractCommand.class.getDeclaredField("description");
    fDesc.setAccessible(true);
    fDesc.set(instance, description);
  }
}
