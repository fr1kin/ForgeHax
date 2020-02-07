package dev.fiki.forgehax.main.util.cmd;

import dev.fiki.forgehax.main.util.cmd.argument.RawArgument;
import dev.fiki.forgehax.main.util.cmd.argument.IArgument;
import dev.fiki.forgehax.main.util.cmd.flag.EnumFlag;
import lombok.Builder;
import lombok.Singular;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ParentCommand extends AbstractParentCommand {
  @Builder
  public ParentCommand(IParentCommand parent,
      String name, @Singular Set<String> aliases, String description,
      @Singular Set<EnumFlag> flags) {
    super(parent, name, aliases, description, flags);
  }
}
