package dev.fiki.forgehax.main.mods.commands;

import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.cmd.argument.Arguments;
import dev.fiki.forgehax.main.util.mod.CommandMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;

import static dev.fiki.forgehax.main.Common.*;

/**
 * Created on 5/27/2017 by fr1kin
 */
@RegisterMod
public class BlocksCommand extends CommandMod {
  
  public BlocksCommand() {
    super("BlocksCommand");
  }

  {
    newSimpleCommand()
        .name("blocks")
        .description("Find block(s) with matching name")
        .argument(Arguments.newStringArgument()
            .label("block")
            .build())
        .executor(args -> {
          String find = args.getFirst().getStringValue();

          args.inform(StreamSupport.stream(getBlockRegistry().spliterator(), false)
              .map(Block::getRegistryName)
              .filter(Objects::nonNull)
              .map(ResourceLocation::toString)
              .filter(block -> block.toLowerCase().contains(find))
              .limit(25)
              .collect(Collectors.joining(", ")));
        })
        .build();
  }
}
