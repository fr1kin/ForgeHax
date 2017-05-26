package com.matt.forgehax;

import com.matt.forgehax.mods.BaseMod;
import com.matt.forgehax.util.blocks.BlockOptionHelper;
import com.matt.forgehax.util.command.Command;
import com.matt.forgehax.util.command.CommandBuilder;
import com.matt.forgehax.util.command.CommandRegistry;
import net.minecraft.block.Block;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.matt.forgehax.Wrapper.*;

/**
 * Created on 5/15/2017 by fr1kin
 */
public class GlobalCommands implements Globals {
    public static void initialize() {
        CommandRegistry.register(new CommandBuilder()
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
        );
        CommandRegistry.register(new CommandBuilder()
                .setName("blocks")
                .setDescription("Lists all blocks with matching name/id")
                .setProcessor(options -> {
                    List<?> args = options.nonOptionArguments();
                    if(args.size() > 0) {
                        final String find = String.valueOf(args.get(0)).toLowerCase();
                        final StringBuilder builder = new StringBuilder("Search results:\n");
                        Block.REGISTRY.forEach(block -> {
                            final AtomicBoolean addedResource = new AtomicBoolean(false);
                            final int id = Block.getIdFromBlock(block);
                            BlockOptionHelper.getAllBlocks(block).forEach(stack -> {
                                String unlocal = stack.getUnlocalizedName();
                                String formal = stack.getDisplayName();
                                if (unlocal.toLowerCase().contains(find) ||
                                        formal.toLowerCase().contains(find)) {
                                    if(addedResource.compareAndSet(false, true)) {
                                        builder.append(String.format("[%03d:%02d] ", id, block.getMetaFromState(block.getDefaultState())));
                                        builder.append(block.getRegistryName() != null ? block.getRegistryName().toString() : block.getLocalizedName());
                                        builder.append('\n');
                                    }
                                    builder.append(String.format("[%03d:%02d]> ", id, stack.getMetadata()));
                                    builder.append(formal);
                                    builder.append(" | ");
                                    builder.append(unlocal);
                                    builder.append('\n');
                                }
                            });
                        });
                        printMessageNaked(builder.toString());
                        return true;
                    } else return false;
                })
                .build()
        );
    }
}
