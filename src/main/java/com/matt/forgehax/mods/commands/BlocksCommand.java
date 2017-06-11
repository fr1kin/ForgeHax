package com.matt.forgehax.mods.commands;

import com.matt.forgehax.util.blocks.BlockOptionHelper;
import com.matt.forgehax.util.command.Command;
import com.matt.forgehax.util.command.CommandBuilder;
import com.matt.forgehax.util.mod.CommandMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.block.Block;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.matt.forgehax.Wrapper.printMessageNaked;

/**
 * Created on 5/27/2017 by fr1kin
 */
@RegisterMod
public class BlocksCommand extends CommandMod {
    public BlocksCommand() {
        super("blocks", "Searches for any blocks that match the given input");
    }

    @Override
    public Command generate(CommandBuilder commandBuilder) {
        return commandBuilder
                .processor(data -> {
                    data.requiredArguments(1);
                    final String find = data.getArgumentAsString(0);
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
                    data.markSuccess();
                })
                .build();
    }
}
