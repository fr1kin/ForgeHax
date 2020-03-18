package dev.fiki.forgehax.main.util.typeconverter.registry;

import net.minecraft.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

public class BlockType extends AbstractRegistryType<Block> {
  @Override
  protected IForgeRegistry<Block> getRegistry() {
    return ForgeRegistries.BLOCKS;
  }
}
