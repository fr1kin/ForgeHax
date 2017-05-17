package com.matt.forgehax;

import com.matt.forgehax.mods.BaseMod;
import com.matt.forgehax.util.command.Command;
import com.matt.forgehax.util.command.CommandBuilder;
import com.matt.forgehax.util.command.CommandRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.io.StringWriter;
import java.util.*;

import static com.matt.forgehax.Wrapper.*;

/**
 * Created on 5/15/2017 by fr1kin
 */
public class GlobalCommands implements Globals {
    public static void initialize() {
        CommandRegistry.registerGlobal(new CommandBuilder()
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
                            BaseMod mod = CommandRegistry.getModByName(name);
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
                            CommandRegistry.getGlobalCommands().forEach((id, cmd) -> {
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
        );

        CommandRegistry.registerGlobal(new CommandBuilder()
                .setName("blocks")
                .setDescription("Lists all blocks with matching name/id")
                .setProcessor(options -> {
                    List<?> args = options.nonOptionArguments();
                    if(args.size() > 0) {
                        final String find = String.valueOf(args.get(0)).toLowerCase();
                        final StringBuilder builder = new StringBuilder("Search results:\n");
                        Block.REGISTRY.forEach(block -> {
                            int id = Block.getIdFromBlock(block);
                            if(block.getLocalizedName().toLowerCase().contains(find) ||
                                    block.getUnlocalizedName().toLowerCase().contains(find) ||
                                    Objects.equals(String.valueOf(id), find)) {
                                builder.append(String.format("[%d] %s\n",
                                        id,
                                        block.getRegistryName() != null ? block.getRegistryName().toString() : block.getLocalizedName())
                                );
                            }
                        });
                        printMessageNaked(builder.toString());
                        return true;
                    } else return false;
                })
                .build()
        );
    }
}
