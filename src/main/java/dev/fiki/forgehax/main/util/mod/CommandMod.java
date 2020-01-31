package dev.fiki.forgehax.main.util.mod;

import com.google.common.collect.Lists;
import dev.fiki.forgehax.main.util.command.Command;
import dev.fiki.forgehax.main.util.command.CommandBuilders;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import joptsimple.internal.Strings;

import static dev.fiki.forgehax.main.Globals.*;

/**
 * Created on 6/1/2017 by fr1kin
 */
public class CommandMod extends ServiceMod {
  
  private final Collection<Command> commands = Lists.newArrayList();
  
  public CommandMod(String name, String desc) {
    super(name, desc);
  }
  
  public CommandMod(String name) {
    super(name, Strings.EMPTY);
  }
  
  @Override
  protected void onLoad() {
    try {
      for (Method m : getClass().getDeclaredMethods()) {
        try {
          m.setAccessible(true);
          if (m.isAnnotationPresent(RegisterCommand.class)
              && Arrays.equals(m.getParameterTypes(), new Class<?>[]{CommandBuilders.class})
              && Command.class.isAssignableFrom(m.getReturnType())) {
            commands.add((Command) m.invoke(this, GLOBAL_COMMAND.builders()));
          }
        } catch (Throwable t) {
          getLogger().error(t);
        }
      }
    } catch (Throwable t) {
      getLogger().error(t);
    }
  }
  
  @Override
  protected void onUnload() {
    commands.forEach(Command::leaveParent);
  }
  
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface RegisterCommand {
  
  }
}
