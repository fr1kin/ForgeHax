package com.matt.forgehax.util.blocks;

import com.google.common.collect.Sets;
import com.matt.forgehax.util.SafeConverter;
import com.matt.forgehax.util.blocks.exceptions.BadBlockEntryFormatException;
import com.matt.forgehax.util.blocks.exceptions.BlockDoesNotExistException;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

/** Created on 5/18/2017 by fr1kin */
public class BlockOptionHelper {
  public static boolean isAir(String name) {
    return Objects.equals(Blocks.AIR.getRegistryName(), new ResourceLocation(name));
  }

  public static boolean isAir(int id) {
    return id == 0;
  }

  public static Collection<ItemStack> getAllBlocks(Block block) {
    NonNullList<ItemStack> list = NonNullList.create();
    if (block != null) {
      block.getSubBlocks(null, list);
    }
    return Collections.unmodifiableCollection(list);
  }

  public static void getAllBlocksMatchingByUnlocalized(
      final Collection<BlockEntry> found, String regex) {
    final Pattern pattern = Pattern.compile(regex);
    Block.REGISTRY.forEach(
        block ->
            getAllBlocks(block)
                .forEach(
                    stack -> {
                      Matcher matcher = pattern.matcher(stack.getUnlocalizedName().toLowerCase());
                      if (matcher.find())
                        try {
                          found.add(new BlockEntry(block, stack.getMetadata(), false));
                        } catch (BlockDoesNotExistException e) {;
                        }
                    }));
  }

  public static Collection<BlockEntry> getAllBlocksMatchingByUnlocalized(String regex) {
    Collection<BlockEntry> map = Sets.newHashSet();
    getAllBlocksMatchingByUnlocalized(map, regex);
    return map;
  }

  public static void getAllBlocksMatchingByLocalized(
      final Collection<BlockEntry> found, String regex) {
    final Pattern pattern = Pattern.compile(regex);
    Block.REGISTRY.forEach(
        block ->
            getAllBlocks(block)
                .forEach(
                    stack -> {
                      Matcher matcher =
                          pattern.matcher(
                              stack.getDisplayName().replaceAll(" ", "_").toLowerCase());
                      if (matcher.find())
                        try {
                          found.add(new BlockEntry(block, stack.getMetadata(), false));
                        } catch (BlockDoesNotExistException e) {;
                        }
                    }));
  }

  public static Collection<BlockEntry> getAllBlocksMatchingByLocalized(String regex) {
    Collection<BlockEntry> map = Sets.newHashSet();
    getAllBlocksMatchingByLocalized(map, regex);
    return map;
  }

  public static Collection<BlockEntry> getAllBlockMatching(String regex) {
    Collection<BlockEntry> map = Sets.newHashSet();
    getAllBlocksMatchingByUnlocalized(map, regex);
    getAllBlocksMatchingByLocalized(map, regex);
    return map;
  }

  public static boolean isValidMetadataValue(Block block, int meta) {
    for (ItemStack stack : getAllBlocks(block)) if (stack.getMetadata() == meta) return true;
    return false;
  }

  public static BlockData fromUniqueName(String uniqueName)
      throws BlockDoesNotExistException, BadBlockEntryFormatException {
    String[] split = uniqueName.split("::");
    if (split.length < 1) throw new BadBlockEntryFormatException();
    String name = split[0];
    int meta = SafeConverter.toInteger(split.length > 1 ? split[1] : -1, -1);
    Block block = Block.getBlockFromName(name);
    if (block == null) throw new BlockDoesNotExistException(uniqueName + " is not a valid block");
    BlockData data = new BlockData();
    data.block = block;
    data.meta = meta;
    return data;
  }

  public static void requiresValidBlock(Block block, int metadataId)
      throws BlockDoesNotExistException {
    if (block == null || block.equals(Blocks.AIR))
      throw new BlockDoesNotExistException("Attempted to create entry for a non-existent block");
    if (!BlockOptionHelper.isValidMetadataValue(block, metadataId))
      throw new BlockDoesNotExistException(
          String.format(
              "Attempted to create entry for block \"%s\" with a invalid meta id of \"%d\"",
              block.getRegistryName().toString(), metadataId));
  }

  public static class BlockData {
    public Block block = null;
    public int meta = -1;
  }
}
