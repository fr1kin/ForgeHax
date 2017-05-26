package com.matt.forgehax.util.blocks;

import com.matt.forgehax.util.blocks.exceptions.BadBlockEntryFormatException;
import com.matt.forgehax.util.blocks.exceptions.BlockDoesNotExistException;
import com.matt.forgehax.util.command.jopt.SafeConverter;
import joptsimple.internal.Strings;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

/**
 * Created on 5/13/2017 by fr1kin
 */
public class BlockEntry extends AbstractBlockEntry {
    public static AbstractBlockEntry create(Block block, int meta, boolean check) throws BlockDoesNotExistException {
        return new BlockEntry(block, meta, check);
    }
    public static AbstractBlockEntry create(Block block, int meta) throws BlockDoesNotExistException {
        return create(block, meta, true);
    }

    public static AbstractBlockEntry createByResource(String name, int meta) throws BlockDoesNotExistException {
        return create(Block.getBlockFromName(name), meta, !BlockOptionHelper.isAir(name));
    }

    public static AbstractBlockEntry createByResource(String domain, String path, int meta) throws BlockDoesNotExistException {
        return createByResource(domain + ":" + path, meta);
    }

    public static AbstractBlockEntry createById(int id, int meta) throws BlockDoesNotExistException {
        return create(Block.getBlockById(id), meta, BlockOptionHelper.isAir(id));
    }

    public static AbstractBlockEntry createByUniqueName(String uniqueName) throws BlockDoesNotExistException, BadBlockEntryFormatException {
        String[] split = uniqueName.split("::");
        if(split.length < 1) throw new BadBlockEntryFormatException();
        String name = split[0];
        int meta = SafeConverter.toInteger(split.length > 1 ? split[1] : -1, -1);
        return createByResource(name, meta);
    }

    private final Block block;
    private final int meta;

    protected BlockEntry(Block block, int meta, boolean validCheck) throws BlockDoesNotExistException {
        meta = Math.max(meta, 0);
        if(validCheck) requiresValidBlock(block, meta);
        this.block = block;
        this.meta = BlockOptionHelper.getAllBlocks(block).size() > 1 ? meta : -1; // if no other variants then don't check metadata
    }

    protected BlockEntry(Block block, int meta) throws BlockDoesNotExistException {
        this(block, meta, true);
    }

    @Override
    public String getUniqueName() {
        return getResourceName() + (isMetadata() ? ("::" + getMetadata()) : Strings.EMPTY);
    }

    @Override
    public String getResourceName() {
        return block.getRegistryName() != null ? block.getRegistryName().toString() : block.toString();
    }

    @Override
    public String getPrettyName() {
        return (block.getRegistryName() != null ? block.getRegistryName().getResourcePath() : block.toString()) + (isMetadata() ? ":" + meta : Strings.EMPTY);
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public int getMetadata() {
        return meta;
    }

    @Override
    public boolean isMetadata() {
        return meta > -1;
    }

    private static void requiresValidBlock(Block block, int metadataId) throws BlockDoesNotExistException {
        if(block == null || block.equals(Blocks.AIR))
            throw new BlockDoesNotExistException("Attempted to create entry for a non-existent block");
        if(!BlockOptionHelper.isValidMetadataValue(block, metadataId))
            throw new BlockDoesNotExistException(String.format("Attempted to create entry for block \"%s\" with a invalid meta id of \"%d\"", block.getRegistryName().toString(), metadataId));
    }
}
