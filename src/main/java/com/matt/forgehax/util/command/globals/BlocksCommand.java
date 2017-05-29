package com.matt.forgehax.util.command.globals;

import com.matt.forgehax.util.blocks.BlockOptionHelper;
import com.matt.forgehax.util.command.Command;
import com.matt.forgehax.util.command.CommandBuilder;
import net.minecraft.block.Block;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.matt.forgehax.Wrapper.printMessageNaked;

/**
 * Created on 5/27/2017 by fr1kin
 */
public class BlocksCommand {
    public static Command newInstance() {
        return new CommandBuilder()
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
                ;
    }
}
