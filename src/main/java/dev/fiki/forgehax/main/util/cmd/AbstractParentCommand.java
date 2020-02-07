package dev.fiki.forgehax.main.util.cmd;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import dev.fiki.forgehax.main.util.cmd.argument.IArgument;
import dev.fiki.forgehax.main.util.cmd.argument.RawArgument;
import dev.fiki.forgehax.main.util.cmd.flag.EnumFlag;
import dev.fiki.forgehax.main.util.cmd.execution.ArgumentList;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractParentCommand extends AbstractCommand implements IParentCommand {
  protected final Set<ICommand> children = Collections.synchronizedSet(Sets.newHashSet());

  public AbstractParentCommand(IParentCommand parent,
      String name, Collection<String> aliases, String description,
      Collection<EnumFlag> flags) {
    super(parent, name, aliases, description, flags);
  }

  protected ICommand getMatchingCommand(String value) {
    if (value.isEmpty()) {
      // not searching for any command, just querying
      return null;
    }

    String lvalue = value.toLowerCase();
    synchronized (children) {
      List<ICommand> matches = Lists.newArrayList();
      for (ICommand command : children) {
        if (value.equalsIgnoreCase(command.getName())) {
          return command;
        } else if (command.getName().toLowerCase().startsWith(lvalue)) {
          matches.add(command);
        }
      }

      if (matches.isEmpty()) {
        throw new IllegalArgumentException("Unknown command");
      } else if (matches.size() == 1) {
        return matches.get(0);
      } else {
        throw new IllegalArgumentException("Ambiguous command: " +
            matches.stream()
                .map(ICommand::getName)
                .collect(Collectors.joining(", ")));
      }
    }
  }

  @Override
  public Collection<ICommand> getChildren() {
    return Collections.unmodifiableCollection(children);
  }

  @Override
  public ICommand getChildByName(String command) {
    synchronized (children) {
      for (ICommand child : children) {
        if (child.getName().equalsIgnoreCase(command)) {
          return child;
        }
      }
      return null;
    }
  }

  @Override
  public List<ICommand> getPossibleMatchingChildren(String search) {
    return getChildren().stream()
        .filter(cmd -> cmd.getName().toLowerCase().startsWith(search.toLowerCase()))
        .sorted(Comparator.<ICommand, Boolean>comparing(cmd -> cmd.getName().equalsIgnoreCase(search))
            .thenComparing(cmd -> cmd.getName().compareToIgnoreCase(search)))
        .collect(Collectors.toList());
  }

  @Override
  public boolean addChild(ICommand command) {
    if (children.add(command)) {
      command.setParent(this);
      return true;
    }
    return false;
  }

  @Override
  public boolean deleteChild(ICommand command) {
    if (children.remove(command)) {
      command.setParent(null);
      return true;
    }
    return false;
  }

  @Override
  public boolean containsChild(ICommand command) {
    return children.contains(command);
  }

  @Override
  public List<IArgument<?>> getArguments() {
    return Collections.singletonList(RawArgument.<ICommand>builder()
        .label("child")
        .type(ICommand.class)
        .parser(this::getMatchingCommand)
        .converter(ICommand::getName)
        .optional()
        .build());
  }

  @Override
  public ICommand onExecute(ArgumentList args) {
    return args.<ICommand>getFirst().getOptionalValue()
        .orElseGet(() -> {
          getChildren().stream()
              .map(ICommand::toString)
              .forEach(s -> args.inform(s));
          return null;
        });
  }
}
