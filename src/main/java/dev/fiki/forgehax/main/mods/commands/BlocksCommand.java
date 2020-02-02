package dev.fiki.forgehax.main.mods.commands;

import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.command.Command;
import dev.fiki.forgehax.main.util.command.CommandBuilders;
import dev.fiki.forgehax.main.util.mod.CommandMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;

/**
 * Created on 5/27/2017 by fr1kin
 */
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

          String find = data.getArgumentAsString(0).toLowerCase();

          data.write(StreamSupport.stream(Common.getBlockRegistry().spliterator(), false)
              .map(Block::getRegistryName)
              .filter(Objects::nonNull)
              .map(ResourceLocation::toString)
              .filter(block -> block.toLowerCase().contains(find))
              .limit(25)
              .collect(Collectors.joining(", ")));

          data.markSuccess();
        })
      .build();
  }
}
