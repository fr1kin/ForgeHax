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

    public static Collection<AbstractBlockEntry> getAllBlocksMatchingByUnlocalized(String regex) {
        final Collection<AbstractBlockEntry> found = Sets.newHashSet();
        final Pattern pattern = Pattern.compile(regex);
        Block.REGISTRY.forEach(block -> getAllBlocks(block).forEach(stack -> {
            Matcher matcher = pattern.matcher(stack.getUnlocalizedName().toLowerCase());
            if(matcher.find()) try {
                found.add(BlockEntry.create(block, stack.getMetadata(), false));
            } catch (BlockDoesNotExistException e) {
                ;
            }
        }));
        return Collections.unmodifiableCollection(found);
    }

    public static Collection<AbstractBlockEntry> getAllBlocksMatchingByLocalized(String regex) {
        final Collection<AbstractBlockEntry> found = Sets.newHashSet();
        final Pattern pattern = Pattern.compile(regex);
        Block.REGISTRY.forEach(block -> getAllBlocks(block).forEach(stack -> {
            Matcher matcher = pattern.matcher(stack.getDisplayName().replaceAll(" ", "_").toLowerCase());
            if(matcher.find()) try {
                found.add(BlockEntry.create(block, stack.getMetadata(), false));
            } catch (BlockDoesNotExistException e) {
                ;
            }
        }));
        return Collections.unmodifiableCollection(found);
    }

    public static AbstractBlockEntry getFirstMatchingByLocalized(String regex) throws BlockDoesNotExistException {
        Collection<AbstractBlockEntry> found = getAllBlocksMatchingByLocalized(regex);
        if(found.size() <= 0) throw new BlockDoesNotExistException(String.format("Could not find block that matches the expression \"%s\"", regex));
        return found.iterator().next();
    }

    public static boolean isValidMetadataValue(Block block, int meta) {
        for(ItemStack stack : getAllBlocks(block)) if(stack.getMetadata() == meta)
            return true;
        return false;
    }
}
