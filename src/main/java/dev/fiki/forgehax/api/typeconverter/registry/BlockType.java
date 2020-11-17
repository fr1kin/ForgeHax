package dev.fiki.forgehax.api.typeconverter.registry;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

public class BlockType extends AbstractRegistryType<Block> {
  @Override
  public IForgeRegistry<Block> getRegistry() {
    return ForgeRegistries.BLOCKS;
  }

  @Override
  public Block parse(String value) {
    Block ret = super.parse(value);
    return (ret == null || ret == Blocks.AIR) ? null : ret;
  }
}
