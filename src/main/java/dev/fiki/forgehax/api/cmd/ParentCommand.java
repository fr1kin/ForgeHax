package dev.fiki.forgehax.api.cmd;

import dev.fiki.forgehax.api.cmd.flag.EnumFlag;
import lombok.Builder;
import lombok.Singular;

import java.util.Set;

public final class ParentCommand extends AbstractParentCommand {
  @Builder
  public ParentCommand(IParentCommand parent,
      String name, @Singular Set<String> aliases, String description,
      @Singular Set<EnumFlag> flags) {
    super(parent, name, aliases, description, flags);
    onFullyConstructed();
  }
}
