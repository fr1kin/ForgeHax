package com.matt.forgehax.util.command.globals;

import com.matt.forgehax.util.mod.BaseMod;
import com.matt.forgehax.util.command.Command;
import com.matt.forgehax.util.command.CommandBuilder;
import com.matt.forgehax.util.command.CommandRegistry;

import java.util.Arrays;
import java.util.Collection;

import static com.matt.forgehax.Wrapper.getModManager;
import static com.matt.forgehax.Wrapper.printMessageNaked;

/**
 * Created on 5/27/2017 by fr1kin
 */
public class HelpCommand {
    public static Command newInstance() {
        return new CommandBuilder()
                .setName("help")
                .setDescription("Lists all the global commands")
                .setOptionBuilder(parser -> {
                    parser.acceptsAll(Arrays.asList("mod", "m"), "Gets info for mod")
                            .withRequiredArg();
                    parser.acceptsAll(Arrays.asList("list", "l"), "Lists all mods or settings within a mod");
                })
                .setProcessor(options -> {
                    final StringBuilder builder = new StringBuilder();
                    final boolean list = options.has("l");
                    if (options.has("mod")) {
                        options.valuesOf("mod").forEach(o -> {
                            String name = String.valueOf(o);
                            BaseMod mod = getModManager().getMod(name);
                            if (mod != null) {
                                builder.append(mod.toString());
                                builder.append('\n');
                                if (list) {
                                    Collection<Command> commands = mod.getCommands();
                                    if (!commands.isEmpty())
                                        commands.forEach(command -> builder.append(String.format("> %s\n", command.toString())));
                                }
                            } else builder.append(String.format("'%s' is not a valid mod\n", name));
                        });
                    } else {
                        if (list) {
                            getModManager().getMods().forEach(mod -> {
                                builder.append(mod.toString());
                                builder.append('\n');
                            });
                        } else {
                            builder.append("Available commands:\n");
                            CommandRegistry.getCommands().forEach((id, cmd) -> {
                                builder.append(cmd.toString());
                                builder.append('\n');
                            });
                            builder.append("You can change a mods setting by typing:\n");
                            builder.append(".<mod> <feature> <value>\n");
                            builder.append("Example: \".step enabled true\"\n");
                            builder.append("ForgeHax uses JOpt for a UNIX command syntax\n");
                            builder.append("Type \".help -?\" for more options for this command\n");
                        }
                    }
                    printMessageNaked(builder.toString());
                    return true;
                })
                .build()
                ;
    }
}
