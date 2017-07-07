package com.matt.forgehax.util.command;

import com.google.common.collect.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.matt.forgehax.Globals;
import com.matt.forgehax.Helper;
import com.matt.forgehax.util.command.callbacks.CallbackData;
import com.matt.forgehax.util.command.exception.CommandBuildException;
import com.matt.forgehax.util.command.exception.CommandExecuteException;
import com.matt.forgehax.util.command.exception.CommandParentNonNullException;
import com.matt.forgehax.util.serialization.GsonConstant;
import com.matt.forgehax.util.serialization.ISerializableJson;
import com.matt.forgehax.util.serialization.ISerializer;
import com.matt.forgehax.util.key.IKeyBind;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.internal.Strings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

/**
 * Created on 5/14/2017 by fr1kin
 */
public class Command implements Comparable<Command>, ISerializer, GsonConstant, ISerializableJson, IKeyBind {
    private static final File SETTINGS_DIR = Helper.getFileManager().getFileInConfigDirectory("settings");

    public static final String NAME             = "Command.name";
    public static final String DESCRIPTION      = "Command.description";
    public static final String OPTIONBUILDERS   = "Command.optionbuilder";
    public static final String PROCESSORS       = "Command.processors";
    public static final String HELP             = "Command.help";
    public static final String PARENT           = "Command.parent";
    public static final String HELPAUTOGEN      = "Command.helpAutoGen";
    public static final String CALLBACKS        = "Command.callbacks";
    public static final String KEYBIND          = "Command.keybind";
    public static final String KEYBIND_OPTIONS  = "Command.keybind_options";

    static {
        SETTINGS_DIR.mkdirs();
    }

    private final String name;
    private final String description;

    protected final OptionParser parser = new OptionParser();

    protected final Collection<Consumer<ExecuteData>> processors = Lists.newArrayList();

    protected final Consumer<ExecuteData> help;

    private final Set<Command> children = Sets.newHashSet();

    protected final Multimap<CallbackType, Consumer<CallbackData>> callbacks = Multimaps.newSetMultimap(Maps.newHashMap(), Sets::newLinkedHashSet);

    //private final Collection<ICommandFlag> flags = Sets.newHashSet();

    private Command parent;
    private final KeyBinding bind;

    @SuppressWarnings("unchecked")
    protected Command(Map<String, Object> data) throws CommandBuildException {
        try {
            this.name =                                     (String)                                data.get(NAME);
            Objects.requireNonNull(this.name, "Command requires name");

            this.description =                              (String)                                data.getOrDefault(DESCRIPTION, Strings.EMPTY);
            this.help =                                     (Consumer<ExecuteData>)                 data.get(HELP);

            // key binding
            Integer keyCode =                               (Integer)                               data.getOrDefault(KEYBIND, -1);
            if(keyCode != -1) {
                bind = new KeyBinding(getAbsoluteName(), keyCode, "ForgeHax");
                ClientRegistry.registerKeyBinding(bind);

                Boolean genOptions =                        (Boolean)                               data.getOrDefault(KEYBIND_OPTIONS, true);
                if(genOptions) {
                    parser.accepts("bind", "Bind to the given key").withRequiredArg();
                    parser.accepts("unbind", "Sets bind to KEY_NONE");

                    this.processors.add(dt -> {
                        if(dt.hasOption("bind")) {
                            String key = dt.getOptionAsString("bind").toUpperCase();

                            if(key.length() < 1 || key.length() > 1) throw new CommandExecuteException("Argument for bind option should be 1 character");

                            int kc = Keyboard.getKeyIndex(key);
                            if(Keyboard.getKeyIndex(key) == Keyboard.KEY_NONE) throw new CommandExecuteException(String.format("\"%s\" is not a valid key name", key));

                            bind(kc);

                            dt.write(String.format("Bound %s to key %s [code=%d]", getAbsoluteName(), key, kc));
                            dt.stopProcessing();
                        } else if(dt.hasOption("unbind")) {
                            unbind();

                            dt.write(String.format("Unbound %s", getAbsoluteName()));
                            dt.stopProcessing();
                        }
                    });
                }
            } else {
                bind = null;
            }

            Collection<Consumer<ExecuteData>> processors =  (Collection<Consumer<ExecuteData>>)     data.get(PROCESSORS);
            if(processors != null) this.processors.addAll(processors);

            // Set command parent
            Command parent =                                (Command)                               data.get(PARENT);
            if(parent != null) parent.addChild(this);

            // By default, auto generate help option
            // User must specify not to add it
            Boolean helpAutoGen =                           (Boolean)                               data.getOrDefault(HELPAUTOGEN, true);
            if(helpAutoGen) {
                parser.acceptsAll(Arrays.asList("help", "?"), "Help text for options");
            }

            // Execute custom option builder (if it exists)
            Collection<Consumer<OptionParser>>  optionBuilders =         (Collection<Consumer<OptionParser>>)           data.get(OPTIONBUILDERS);
            if(optionBuilders != null) {
                for(Consumer<OptionParser> c : optionBuilders) c.accept(this.parser);
            }

            // Add any callbacks created by the builder
            Multimap<CallbackType, Consumer<CallbackData>> callbacks =       (Multimap<CallbackType, Consumer<CallbackData>>)     data.get(CALLBACKS);
            if(callbacks != null) {
                this.callbacks.putAll(callbacks);
            }
        } catch (Throwable t) {
            throw new CommandBuildException("Failed to build command", t);
        }
    }

    public boolean isGlobal() {
        return false;
    }

    public String getName() {
        return name;
    }

    public String getAbsoluteName() {
        return (getParent() != null && !getParent().isGlobal()) ? (getParent().getAbsoluteName() + "." + getName()) : getName();
    }

    public String getDescription() {
        return description;
    }

    public String getPrintText() {
        return getAbsoluteName();
    }

    public String getOptionHelpText() {
        StringWriter writer = new StringWriter();
        try {
            parser.printHelpOn(writer);
        } catch (IOException e) {
            ;
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                ;
            }
        }
        return writer.toString();
    }

    @Nullable
    public Command getParent() {
        return parent;
    }

    protected void setParent(Command parent) {
        if(this.parent != null && parent != null) throw new CommandParentNonNullException("Command parent already exists");
        this.parent = parent;
    }

    public boolean leaveParent() {
        return parent != null && parent.removeChild(this);
    }

    public CommandBuilders builders() {
        return CommandBuilders.newInstance(this);
    }

    public boolean addChild(@Nonnull Command child) {
        boolean b;
        if(b = children.add(child)) child.setParent(this);
        return b;
    }

    public boolean removeChild(@Nonnull Command child) {
        boolean b;
        // if child was removed, set parent to null.
        if(b = children.remove(child)) child.setParent(null);
        return b;
    }

    @Nullable
    public Command getChild(String name) {
        for(Command command : children) if(command.getName().equalsIgnoreCase(name))
            return command;
        return null;
    }

    public Collection<Command> getChildren() {
        return Collections.unmodifiableCollection(children);
    }

    public void getChildrenDeep(final Collection<Command> all) {
        all.addAll(getChildren());
        children.forEach(child -> child.getChildrenDeep(all));
    }

    public Collection<Command> getChildrenDeep() {
        Collection<Command> all = Sets.newHashSet();
        getChildrenDeep(all);
        return all; // does not need to be unmodifiable since we are creating a duplicate containing all children
    }

    public void abandonChildren() {
        children.forEach(Command::leaveParent);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    protected <T extends CallbackData> Consumer<T> addCallback(CallbackType type, Consumer<T> consumer) {
        return callbacks.put(type, (Consumer<CallbackData>) consumer) ? consumer : null;
    }

    protected boolean removeCallback(CallbackType type, Consumer<? extends CallbackData> consumer) {
        return callbacks.remove(type, consumer);
    }

    protected <T extends CallbackData> void invokeCallbacks(CallbackType type, T data) {
        callbacks.get(type).forEach(c -> c.accept(data));
    }

    protected boolean processHelp(ExecuteData data) throws CommandExecuteException, NullPointerException {
        if(help != null) {
            help.accept(data);
            return true;
        } else if(data.options().has("help")) {
            Helper.printMessageNaked(getOptionHelpText());
            return true;
        } else return false;
    }

    protected boolean processMain(ExecuteData data) throws CommandExecuteException, NullPointerException {
        if(processors != null) {
            for(Consumer<ExecuteData> c : processors) try {
                c.accept(data);
                if(data.isStopped()) break;
            } catch (Throwable t) {
                data.markFailed();
                throw t;
            }
            return true;
        } else return false;
    }

    protected boolean processChildren(@Nonnull String[] args) throws CommandExecuteException, NullPointerException {
        if (args.length > 0) {
            Command child = getChild(args[0]);
            if(child != null) {
                child.run(CommandHelper.forward(args));
                return true;
            }
        }
        return false;
    }

    public void run(@Nonnull String[] args) throws CommandExecuteException, NullPointerException {
        if (!processChildren(args)) { // attempt to match child commands first
            OptionSet options = parser.parse(args);
            ExecuteData data = new ExecuteData(this, options);

            // only process main if no help was processed
            if (!processHelp(data)) processMain(data);

            switch (data.state()) {
                case SUCCESS:
                    invokeCallbacks(CallbackType.SUCCESS, new CallbackData(this));
                    break;
                case FAILED:
                    invokeCallbacks(CallbackType.FAILURE, new CallbackData(this));
                    break;
            }
        }
    }

    private Path getSettingsPath() {
        return new File(SETTINGS_DIR, getAbsoluteName() + ".json").toPath();
    }

    @Override
    public void serialize(JsonWriter writer) throws IOException {
        writer.beginObject();

        writer.name("bind");
        if(getBind() == null)
            writer.value(-1);
        else
            writer.value(getBind().getKeyCode());

        writer.endObject();
    }

    @Override
    public void deserialize(JsonReader reader) throws IOException {
        reader.beginObject();

        reader.nextName(); // bind
        bind(reader.nextInt());

        reader.endObject();
    }

    @Override
    public void serialize() {
        Path path = getSettingsPath();
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        try {
            writer.beginArray();
            serialize(writer);
            writer.endArray();

            Files.write(path, sw.toString().getBytes());
        } catch (Throwable t) {
            Helper.printStackTrace(t);
            Globals.LOGGER.warn(String.format("Could not serialize \"%s\": %s", getAbsoluteName(), t.getMessage()));
        } finally {
            try {
                sw.close();
            } catch (IOException e) {
                ;
            } finally {
                try {
                    writer.close();
                } catch (IOException e) {
                    ;
                }
            }
        }
    }

    public void serializeAll() {
        serialize();
        getChildren().forEach(Command::serializeAll);
    }

    @Override
    public void deserialize() {
        Path path = getSettingsPath();
        StringReader sr = null;
        JsonReader reader = null;
        if(Files.exists(path)) try {
            sr = new StringReader(new String(Files.readAllBytes(path)));
            reader = new JsonReader(sr);

            reader.beginArray();
            deserialize(reader);
            reader.endArray();
        } catch (Throwable t) {
            Helper.printStackTrace(t);
            Globals.LOGGER.warn(String.format("Could not deserialize \"%s\": %s", getAbsoluteName(), t.getMessage()));
            // incompatible file, will just keep causing errors
            try {
                Files.delete(path);
            } catch (IOException e) {}
        } finally {
            if(sr != null) sr.close();
            try {
                if(reader != null) reader.close();
            } catch (IOException e) {
                ;
            }
        }
    }

    public void deserializeAll() {
        deserialize();
        getChildren().forEach(Command::deserializeAll);
    }

    @Override
    public void bind(int keyCode) {
        if(bind != null) {
            bind.setKeyCode(keyCode);
            KeyBinding.resetKeyBindingArrayAndHash();
        }
    }

    @Nullable
    public KeyBinding getBind() {
        return bind;
    }

    @Override
    public void onKeyPressed() {
        invokeCallbacks(CallbackType.KEY_PRESSED, new CallbackData(this));
    }

    @Override
    public void onKeyDown() {
        invokeCallbacks(CallbackType.KEY_DOWN, new CallbackData(this));
    }

    @Override
    public int compareTo(Command o) {
        return String.CASE_INSENSITIVE_ORDER.compare(getAbsoluteName(), o.getAbsoluteName());
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Command && getAbsoluteName().equalsIgnoreCase(((Command) o).getAbsoluteName());
    }

    @Override
    public int hashCode() {
        return getAbsoluteName().toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return getAbsoluteName();
    }
}
