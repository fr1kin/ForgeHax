package com.matt.forgehax.util.blocks;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.blocks.options.BlockBoundOption;
import com.matt.forgehax.util.blocks.options.BlockColorOption;
import com.matt.forgehax.util.blocks.options.IBlockOption;
import com.matt.forgehax.util.json.ISerializableJson;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import scala.actors.threadpool.Arrays;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Created on 5/19/2017 by fr1kin
 */
public abstract class AbstractBlockEntry implements ISerializableJson {
    private final List<IBlockOption> options = Lists.newArrayList();

    private final BlockColorOption colorOption = new BlockColorOption();
    private final BlockBoundOption boundOption = new BlockBoundOption();

    protected AbstractBlockEntry() {
        registerOption(colorOption);
        registerOption(boundOption);
    }

    protected void registerOption(IBlockOption option) {
        options.add(option);
    }

    public abstract String getUniqueName();

    public abstract String getResourceName();

    public abstract String getPrettyName();

    public abstract Block getBlock();

    public abstract int getMetadata();

    public abstract boolean isMetadata();

    public BlockColorOption getColor() {
        return colorOption;
    }

    public BlockBoundOption getBounds() {
        return boundOption;
    }

    boolean isEqual(Block block, int meta) {
        return Objects.equals(getBlock(), block) && (!isMetadata() || (getMetadata() == meta));
    }

    @Override
    public void serialize(final JsonObject head) {
        final JsonObject entry = new JsonObject();
        options.forEach(option -> option.serialize(entry));
        head.add(getUniqueName(), entry);
    }

    @Override
    public void deserialize(final JsonObject head) {
        if(head.has(getUniqueName())) try {
            final JsonObject entry = head.get(getUniqueName()).getAsJsonObject();
            options.forEach(option -> option.deserialize(entry));
        } catch (Exception e) {
            ;
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AbstractBlockEntry && isEqual(((AbstractBlockEntry) obj).getBlock(), ((AbstractBlockEntry) obj).getMetadata());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getPrettyName());
        builder.append(" {");
        Iterator<IBlockOption> it = options.iterator();
        while(it.hasNext()) {
            IBlockOption option = it.next();
            builder.append(option.toString());
            if(it.hasNext()) builder.append(", ");
        }
        builder.append("}");
        return builder.toString();
    }
}
