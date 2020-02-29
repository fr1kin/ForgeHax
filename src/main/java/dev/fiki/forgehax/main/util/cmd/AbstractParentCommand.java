package dev.fiki.forgehax.main.util.cmd;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import dev.fiki.forgehax.main.util.cmd.argument.IArgument;
import dev.fiki.forgehax.main.util.cmd.execution.ArgumentList;
import dev.fiki.forgehax.main.util.cmd.flag.EnumFlag;
import lombok.AllArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractParentCommand extends AbstractCommand implements IParentCommand {
  protected final Map<String, ICommand> subCommands =
      Collections.synchronizedMap(Maps.newTreeMap(String.CASE_INSENSITIVE_ORDER));

  public AbstractParentCommand(IParentCommand parent,
      String name, Collection<String> aliases, String description,
      Collection<EnumFlag> flags) {
    super(parent, name, aliases, description, flags);
  }

  protected void checkForNameConflicts(ICommand command) {
    for (String name : command.getNameAndAliases()) {
      ICommand cmd = subCommands.get(name);
      if (cmd != null) {
        throw new IllegalArgumentException("Name \"" + name
            + "\" from command \""
            + command.getName()
            + "\" conflicts with existing child command \""
            + cmd.getName());
      }
    }
  }

  @Override
  public Collection<ICommand> getChildren() {
    return Collections.unmodifiableCollection(Sets.newLinkedHashSet(subCommands.values()));
  }

  @Override
  public ICommand getChildByName(String command) {
    return subCommands.get(command);
  }

  @Override
  public List<ICommand> getPossibleMatchingChildren(String search) {
    final String searchLc = search.toLowerCase();
    ICommand child = getChildByName(search);
    if (child != null) {
      // if there is a direct match, then get that
      return Collections.singletonList(child);
    } else {
      // otherwise try and find a list of possible matches
      return subCommands.entrySet().stream()
          .filter(entry -> entry.getKey().toLowerCase().startsWith(searchLc))
          .sorted(Map.Entry.comparingByKey(Comparator.comparingInt(String::length)
              .thenComparing(String::compareToIgnoreCase)))
          .map(Map.Entry::getValue)
          .distinct()
          .collect(Collectors.toList());
    }
  }

  @Override
  public boolean addChild(ICommand command) {
    checkForNameConflicts(command);

    // add all known names
    for (String name : command.getNameAndAliases()) {
      subCommands.put(name, command);
    }

    // set parent
    command.setParent(this);
    return true;
  }

  @Override
  public boolean deleteChild(ICommand command) {
    if (containsChild(command)) {
      // remove all names from the map
      for (String name : command.getNameAndAliases()) {
        subCommands.remove(name);
      }

      // set commands parent to null
      command.setParent(null);
      return true;
    }
    return false;
  }

  @Override
  public boolean containsChild(ICommand command) {
    return subCommands.containsValue(command);
  }

  @Override
  public List<IArgument<?>> getArguments() {
    return Collections.singletonList(new ParentCommandArgument(this, "subcommand"));
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

  @AllArgsConstructor
  public static class ParentCommandArgument implements IArgument<ICommand> {
    private final IParentCommand command;
    private final String label;

    @Override
    public String getLabel() {
      return label;
    }

    @Override
    public ICommand getDefaultValue() {
      return null;
    }

    @Override
    public int getMinArgumentsConsumed() {
      return 0;
    }

    @Override
    public int getMaxArgumentsConsumed() {
      return 1;
    }

    @Override
    public Class<ICommand> type() {
      return ICommand.class;
    }

    @Override
    public ICommand parse(String value) {
      if (value.isEmpty()) {
        // not searching
        return null;
      }

      List<ICommand> matches = command.getPossibleMatchingChildren(value);
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

    @Override
    public String convert(ICommand value) {
      return value.getName();
    }

    @Override
    public Comparator<ICommand> comparator() {
      return CommandHelper.commandComparator();
    }
  }
}
