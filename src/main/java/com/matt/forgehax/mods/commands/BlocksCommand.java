package com.matt.forgehax.mods.commands;

import com.matt.forgehax.util.command.Command;
import com.matt.forgehax.util.command.CommandBuilders;
import com.matt.forgehax.util.mod.CommandMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;

import static com.matt.forgehax.Globals.*;

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

          data.write(StreamSupport.stream(getBlockRegistry().spliterator(), false)
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
