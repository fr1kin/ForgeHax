package com.matt.forgehax.mods.commands;

import com.matt.forgehax.util.command.Command;
import com.matt.forgehax.util.command.CommandBuilders;
import com.matt.forgehax.util.mod.BaseMod;
import com.matt.forgehax.util.mod.CommandMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;

import java.util.Arrays;
import java.util.Collection;

import static com.matt.forgehax.Helper.getModManager;
import static com.matt.forgehax.Helper.printMessageNaked;

/**
 * Created on 6/1/2017 by fr1kin
 */
@RegisterMod
public class HelpCommand extends CommandMod {
    public HelpCommand() {
        super("HelpCommand");
    }

    @RegisterCommand
    public Command help(CommandBuilders builder) {
        return builder.newCommandBuilder()
                .name("help")
                .description("Help text for mod syntax and command list")
                .options(parser -> {
                    parser.acceptsAll(Arrays.asList("mod", "m"), "Gets info for mod")
                            .withRequiredArg();
                    parser.acceptsAll(Arrays.asList("list", "l"), "Lists all mods or settings within a mod");
                })
                .processor(data -> {
                    final StringBuilder build = new StringBuilder();
                    final boolean list = data.hasOption("l");
                    if (data.hasOption("mod")) {
                        data.getOptions("mod").forEach(o -> {
                            String name = String.valueOf(o);
                            BaseMod mod = getModManager().getMod(name);
                            if (mod != null) {
                                build.append(mod.toString());
                                build.append('\n');
                                if (list) {
                                    Collection<Command> commands = mod.getCommands();
                                    if (!commands.isEmpty())
                                        commands.forEach(command -> build.append(String.format("> %s\n", command.toString())));
                                }
                            } else build.append(String.format("'%s' is not a valid mod\n", name));
                        });
                    } else {
                        if (list) {
                            getModManager().getMods().forEach(mod -> {
                                build.append(mod.toString());
                                build.append('\n');
                            });
                        } else {
                            build.append("Available commands:\n");
                            GLOBAL_COMMAND.getChildren().forEach(command -> {
                                build.append(command.toString());
                                build.append('\n');
                            });
                            build.append("You can change a mods setting by typing:\n");
                            build.append(".<mod> <feature> <value>\n");
                            build.append("Example: \".step enabled true\"\n");
                            build.append("Type \".help -?\" for more options for this command\n");
                        }
                    }
                    printMessageNaked(build.toString());
                    data.markSuccess();
                })
                .build();
    }
}
