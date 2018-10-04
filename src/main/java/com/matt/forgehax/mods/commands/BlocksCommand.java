package com.matt.forgehax.mods.commands;

import com.matt.forgehax.util.blocks.BlockOptionHelper;
import com.matt.forgehax.util.command.Command;
import com.matt.forgehax.util.command.CommandBuilders;
import com.matt.forgehax.util.mod.CommandMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.block.Block;

/** Created on 5/27/2017 by fr1kin */
@RegisterMod
public class BlocksCommand extends CommandMod {
  public BlocksCommand() {
    super("BlocksCommand");
  }

  @RegisterCommand
  public Command blocks(CommandBuilders builders) {
    return builders
        .newCommandBuilder()
        .name("blocks")
        .description("Find block(s) with matching name")
        .processor(
            data -> {
              data.requiredArguments(1);
              final String find = data.getArgumentAsString(0).toLowerCase();
              final StringBuilder builder = new StringBuilder("Search results:\n");
              Block.REGISTRY.forEach(
                  block -> {
                    final int id = Block.getIdFromBlock(block);
                    final boolean resourceMatches =
                        block.getRegistryName() != null
                            && block.getRegistryName().toString().toLowerCase().contains(find);
                    final AtomicBoolean addedResource = new AtomicBoolean(resourceMatches);
                    BlockOptionHelper.getAllBlocks(block)
                        .forEach(
                            stack -> {
                              String localized = stack.getDisplayName();
                              String unlocalized = stack.getUnlocalizedName();
                              if (resourceMatches
                                  || unlocalized.toLowerCase().contains(find)
                                  || localized.toLowerCase().contains(find)) {
                                if (addedResource.compareAndSet(false, true)) {
                                  builder.append(
                                      String.format(
                                          "[%03d:%02d] ",
                                          id, block.getMetaFromState(block.getDefaultState())));
                                  builder.append(
                                      block.getRegistryName() != null
                                          ? block.getRegistryName().toString()
                                          : block.getLocalizedName());
                                  builder.append('\n');
                                }
                                builder.append(
                                    String.format("[%03d:%02d]> ", id, stack.getMetadata()));
                                builder.append(localized);
                                builder.append(" | ");
                                builder.append(unlocalized);
                                builder.append('\n');
                              }
                            });
                  });
              data.write(builder.toString());
              data.markSuccess();
            })
        .build();
  }
}
