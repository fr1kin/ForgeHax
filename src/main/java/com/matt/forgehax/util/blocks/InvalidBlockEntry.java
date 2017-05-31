package com.matt.forgehax.util.blocks;

import com.matt.forgehax.util.blocks.exceptions.BadBlockEntryFormatException;
import com.matt.forgehax.util.jopt.SafeConverter;
import net.minecraft.block.Block;

/**
 * Created on 5/19/2017 by fr1kin
 *
 * Used to keep blocks from other mods in the list even after removing the mod
 */
public class InvalidBlockEntry extends AbstractBlockEntry {
    public static AbstractBlockEntry createByUniqueName(String uniqueName) throws BadBlockEntryFormatException {
        return new InvalidBlockEntry(uniqueName);
    }

    private final String uniqueName;
    private final String name;
    private final int meta;

    public InvalidBlockEntry(String uniqueName) throws BadBlockEntryFormatException {
        this.uniqueName = uniqueName;
        String[] split = uniqueName.split("::");
        if(split.length < 2) throw new BadBlockEntryFormatException();
        this.name = split[0];
        this.meta = SafeConverter.toInteger(split[1], 0);
    }

    @Override
    public String getUniqueName() {
        return uniqueName;
    }

    @Override
    public String getResourceName() {
        return name;
    }

    @Override
    public String getPrettyName() {
        return null;
    }

    @Override
    public Block getBlock() {
        return null;
    }

    @Override
    public int getMetadata() {
        return meta;
    }

    @Override
    public boolean isMetadata() {
        return meta > -1;
    }
}
