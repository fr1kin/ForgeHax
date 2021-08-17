package dev.fiki.forgehax.main.commands;

// TODO: hold macros
/*
@RegisterMod
public class MacroCommand extends CommandMod {
  
  public MacroCommand() {
    super("MacroCommand");
  }
  
  public final Options<MacroEntry> MACROS =
    GLOBAL_COMMAND
      .builders()
      .<MacroEntry>newOptionsBuilder()
      .name("macros")
      .description("Registered macros")
      .supplier(ArrayList::new)
      .factory(MacroEntry::new)
      .build();
  
  // which command in the list of commands to execute next
  private final Map<MacroEntry, Integer> macroIndex = new HashMap<>();
  
  @SubscribeEvent
  public void onKeyboardEvent(InputEvent.KeyInputEvent event) {
    MACROS.stream()
        .filter(macro -> !macro.isAnonymous())
        .filter(macro -> macro.getBind().isPressed())
        .forEach(this::executeMacro);
    
    // execute anonymous macros
    MACROS.stream()
        .filter(MacroEntry::isAnonymous)
        .filter(macro -> macro.getKey() == event.getKey())
        .forEach(this::executeMacro);
  }
  
  private void removeMacro(MacroEntry macro) {
    MACROS.remove(macro);
    
    if (macro.name.isPresent()) {
      BindingHelper.removeBinding(macro.getBind());
    }

    // remove the category if there are no named macros to prevent crash
    // TODO: fix crash when a category is empty
    if (MACROS.stream().allMatch(MacroEntry::isAnonymous)) {
      //KeyBinding.getKeybinds().remove("Macros");
      //TODO: 1.15
    }
  }
  
  @Override
  public void onLoad() {
    MACROS.deserializeAll();
    
    MACROS
      .builders()
      .newCommandBuilder()
      .name("remove")
      .description("Remove a macro (Usage: \".macros remove --name hack (and/or) --key f\") ")
      .options(MacroBuilders::keyOption)
      .options(MacroBuilders::nameOption)
      .processor(data -> {
        if (!data.hasOption("key") && !data.hasOption("name")) {
          // jopt doesn't seem to allow options to depend on each other
          throw new CommandExecuteException("Missing required option(s) [k/key], [n/name]");
        }
        
        if (data.hasOption("key")) {
          // remove by key
          final int key = InputMappings.getInputByName(data.getOptionAsString("key")).getValue();
          MACROS
            .stream()
            .filter(macro -> macro.getKey() == key)
            .peek(__ -> printInform("Removing bind for key \"%s\"", key))
            .forEach(this::removeMacro);
        }
        if (data.hasOption("name")) {
          // remove by name
          final String name = data.getOptionAsString("name");
          MACROS
            .stream()
            .filter(macro -> macro.getName().map(name::equals).orElseGet(name::isEmpty))
            .peek(__ -> printInform("Removing bind \"%s\"", name))
            .forEach(this::removeMacro);
        }
      })
      .build();
    
    MACROS
      .builders()
      .newCommandBuilder()
      .name("list")
      .description("List all the macros")
      .options(MacroBuilders::fullOption)
      .processor(data -> {
        printInform("Macros (%d):", MACROS.size());
        for (MacroEntry macro : MACROS) {
          data.write(
            macro.name.map(name -> '\"' + name + '\"').orElse("anonymous") + ": " + Keyboard
              .getKeyName(macro.key));
          if (data.hasOption("full")) {
            data.incrementIndent();
            data.write(this.rawMacroString(macro));
            data.decrementIndent();
          }
        }
      })
      .build();
  }
  
  // TODO: split into separate lines if too long
  private String rawMacroString(MacroEntry macro) {
    return macro.commands.stream()
      .map(nested ->
        nested.stream()
          .collect(Collectors.joining(";", "(", ")"))
      )
      .collect(Collectors.joining(" "));
  }
  
  @Override
  public void onUnload() {
    MACROS.serializeAll();
  }
  
  @RegisterCommand
  public Command executeMacro(CommandBuilders builder) {
    return builder
      .newCommandBuilder()
      .name("exec")
      .description("Execute a named macro")
      .requiredArgs(1)
      .processor(
        data -> {
          final String name = data.getArgumentAsString(0);
          final MacroEntry macro =
            MACROS
              .stream()
              .filter(entry -> entry.getName().map(name::equals).orElse(false))
              .findFirst()
              .orElseThrow(
                () -> new CommandExecuteException(String.format("Unknown macro: \"%s\"", name)));
          
          executeMacro(macro);
        })
      .build();
  }
  
  private void executeMacro(MacroEntry macro) {
    final int currentIndex = Optional.ofNullable(macroIndex.putIfAbsent(macro, 0)).orElse(0);
    macro.getCommands().get(currentIndex).forEach(ChatCommandService::handleCommand);
    macroIndex.replace(macro, rotate(currentIndex, 0, macro.getCommands().size() - 1));
  }
  
  private int rotate(int i, int min, int max) {
    return (i >= max) ? min : i + 1;
  }
  
  @RegisterCommand
  public Command bindMacro(CommandBuilders builder) {
    return builder
      .newCommandBuilder()
      .name("bindmacro")
      .description("Usage: .bindmacro f \"clip 5; <command>; ...\" --name cool_macro")
      .options(MacroBuilders::nameOption)
      .processor(MacroBuilders::parseName)
      .processor(
        data -> {
          data.requiredArguments(2);
          final int key = Keyboard.getKeyIndex(data.getArgumentAsString(0).toUpperCase());
          if (data.getOption("name") == null && key == Keyboard.KEY_NONE) {
            throw new CommandExecuteException("A macro must have a name and/or a valid key");
          }
          
          final List<ImmutableList<String>> commands =
            data.arguments()
              .stream()
              .skip(1) // skip key
              .map(Object::toString)
              .map(this::parseCommand)
              .collect(Collectors.toList());
          
          final MacroEntry macro =
            new MacroEntry((String) data.getOption("name"), key, commands);
          MACROS
            .stream()
            .filter(m -> m.getName().isPresent() && m.getName().equals(macro.getName()))
            .findFirst()
            .ifPresent(alreadyExists -> {
              throw new CommandExecuteException(
                String.format("Command \"%s\" already exists!", alreadyExists.getName().get()));
            });
          MACROS.add(macro);
          
          if (!macro.isAnonymous()) {
            macro.registerBind();
          }
          
          Helper.printMessage("Successfully bound to %s", Keyboard.getKeyName(key));
        })
      .build();
  }
  
  private ImmutableList<String> parseCommand(String input) {
    return ImmutableList.copyOf(Arrays.asList(
      input.split(";"))); // TODO: don't split semicolons in quotes and allow escaped semicolons
  }
  
  public static class MacroEntry implements ISerializableJson {
    
    private Optional<String> name;
    private int key = Keyboard.KEY_NONE;
    private final List<ImmutableList<String>> commands = new ArrayList<>();
    
    @Nullable // null if this is an anonymous macro (ie !name.isPresent())
    private transient KeyBinding bind;
    
    public MacroEntry(String name) {
      this.name = name.isEmpty() ? Optional.empty() : Optional.of(name);
    }
    
    public MacroEntry(@Nullable String name, int key, List<ImmutableList<String>> commands) {
      this.name = Optional.ofNullable(name);
      this.key = key;
      this.commands.addAll(commands);
    }
    
    public int getKey() {
      return Optional.ofNullable(bind).map(KeyBinding::getValue).orElse(this.key);
    }
    
    public Optional<String> getName() {
      return this.name;
    }
    
    public boolean isAnonymous() {
      return !getName().isPresent();
    }
    
    public List<ImmutableList<String>> getCommands() {
      return commands;
    }
    
    public KeyBinding getBind() {
      return this.bind;
    }
    
    // only done for named macros
    private void registerBind() {
      KeyBinding bind = new KeyBinding(name.get(), this.getKey(), "Macros");
      ClientRegistry.registerKeyBinding(bind); // TODO: listen for key pressed for anonymous macros
      this.bind = bind;
    }
    
    @Override
    public void serialize(JsonWriter writer) throws IOException {
      writer.beginObject();
      writer.name("key");
      writer.value(getKey());
      
      writer.name("commands");
      writer.beginArray();
      for (final List<String> list : commands) {
        writer.beginArray();
        for (final String cmd : list) {
          writer.value(cmd);
        }
        writer.endArray();
      }
      writer.endArray();
      writer.endObject();
    }
    
    @Override
    public void deserialize(JsonReader reader) throws IOException {
      JsonObject root = new JsonParser().parse(reader).getAsJsonObject();
      this.key = root.get("key").getAsInt();
      
      Streams.stream(root.get("commands").getAsJsonArray())
        .map(JsonElement::getAsJsonArray)
        .map(jArray -> ImmutableList.copyOf(stringIterator(jArray)))
        .forEach(commands::add);
      
      if (!this.isAnonymous()) {
        this.registerBind();
      }
    }
    
    @Override
    public String toString() {
      return name.orElse("");
    }
  }
  
  private static Iterator<String> stringIterator(JsonArray jsonArray) {
    return Streams.stream(jsonArray).map(JsonElement::getAsString).iterator();
  }
  
  private static class MacroBuilders {
    
    static void nameOption(OptionParser parser) {
      parser.acceptsAll(Arrays.asList("name", "n"), "name").withRequiredArg();
    }
    
    static void keyOption(OptionParser parser) {
      parser.acceptsAll(Arrays.asList("key", "k"), "key").withRequiredArg();
    }
    
    static void fullOption(OptionParser parser) {
      parser.accepts("full").withOptionalArg();
    }
    
    static void parseName(ExecuteData data) {
      final @Nullable String name = (String) data.getOption("name");
      data.set("name", Optional.ofNullable(name));
    }
  }
}
*/
