package com.matt.forgehax.util.blocks;

import com.google.common.collect.Sets;
import com.matt.forgehax.util.blocks.exceptions.BlockDoesNotExistException;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created on 5/18/2017 by fr1kin
 */
public class BlockOptionHelper {
    public static boolean isAir(String name) {
        return Objects.equals(Blocks.AIR.getRegistryName(), new ResourceLocation(name));
    }

    public static boolean isAir(int id) {
        return id == 0;
    }

    public static Collection<ItemStack> getAllBlocks(Block block) {
        NonNullList<ItemStack> list = NonNullList.create();
        if(block != null) {
            block.getSubBlocks(Item.getItemFromBlock(block), null, list);
        }
        return Collections.unmodifiableCollection(list);
    }

    public static void getAllBlocksMatchingByUnlocalized(final Collection<AbstractBlockEntry> found, String regex) {
        final Pattern pattern = Pattern.compile(regex);
        Block.REGISTRY.forEach(block -> getAllBlocks(block).forEach(stack -> {
            Matcher matcher = pattern.matcher(stack.getUnlocalizedName().toLowerCase());
            if(matcher.find()) try {
                found.add(BlockEntry.create(block, stack.getMetadata(), false));
            } catch (BlockDoesNotExistException e) {
                ;
            }
        }));
    }

    public static Collection<AbstractBlockEntry> getAllBlocksMatchingByUnlocalized(String regex) {
        Collection<AbstractBlockEntry> map = Sets.newHashSet();
        getAllBlocksMatchingByUnlocalized(map, regex);
        return map;
    }

    public static void getAllBlocksMatchingByLocalized(final Collection<AbstractBlockEntry> found, String regex) {
        final Pattern pattern = Pattern.compile(regex);
        Block.REGISTRY.forEach(block -> getAllBlocks(block).forEach(stack -> {
            Matcher matcher = pattern.matcher(stack.getDisplayName().replaceAll(" ", "_").toLowerCase());
            if(matcher.find()) try {
                found.add(BlockEntry.create(block, stack.getMetadata(), false));
            } catch (BlockDoesNotExistException e) {
                ;
            }
        }));
    }

    public static Collection<AbstractBlockEntry> getAllBlocksMatchingByLocalized(String regex) {
        Collection<AbstractBlockEntry> map = Sets.newHashSet();
        getAllBlocksMatchingByLocalized(map, regex);
        return map;
    }

    public static Collection<AbstractBlockEntry> getAllBlockMatching(String regex) {
        Collection<AbstractBlockEntry> map = Sets.newHashSet();
        getAllBlocksMatchingByUnlocalized(map, regex);
        getAllBlocksMatchingByLocalized(map, regex);
        return map;
    }

    public static boolean isValidMetadataValue(Block block, int meta) {
        for(ItemStack stack : getAllBlocks(block)) if(stack.getMetadata() == meta)
            return true;
        return false;
    }
}
