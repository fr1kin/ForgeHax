package com.matt.forgehax.util.blocks;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.matt.forgehax.util.blocks.properties.*;
import com.matt.forgehax.util.json.ISerializableJson;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

/**
 * Created on 5/19/2017 by fr1kin
 */
public abstract class AbstractBlockEntry implements ISerializableJson {
    private final BlockBoundProperty boundProperty = new BlockBoundProperty();
    private final BlockColorProperty colorProperty = new BlockColorProperty();
    private final BlockDimensionProperty dimensionProperty = new BlockDimensionProperty();
    private final BlockTagProperty tagProperty = new BlockTagProperty();
    private final BlockToggleProperty toggleProperty = new BlockToggleProperty();

    public Collection<IBlockProperty> getProperties() {
        List<IBlockProperty> properties = Lists.newArrayList();
        properties.add(colorProperty);
        properties.add(boundProperty);
        properties.add(dimensionProperty);
        properties.add(tagProperty);
        properties.add(toggleProperty);
        return Collections.unmodifiableList(properties);
    }

    public abstract String getUniqueName();

    public abstract String getResourceName();

    public abstract String getPrettyName();

    public abstract Block getBlock();

    public abstract int getMetadata();

    public abstract boolean isMetadata();

    public BlockColorProperty getColor() {
        return colorProperty;
    }

    public BlockBoundProperty getBounds() {
        return boundProperty;
    }

    boolean isEqual(Block block, int meta) {
        return Objects.equals(getBlock(), block) && (!isMetadata() || (getMetadata() == meta));
    }
    
    boolean shouldProcess(IBlockState state, BlockPos pos, int dimension) {
        return isEqual(state.getBlock(), state.getBlock().getMetaFromState(state)) &&
                toggleProperty.isEnabled() &&
                boundProperty.isWithinBoundaries(pos.getY()) &&
                dimensionProperty.isValidDimension(dimension);
                // TODO: check tag
    }

    @Override
    public void serialize(final JsonObject head) {
        final JsonObject entry = new JsonObject();
        getProperties().forEach(option -> option.serialize(entry));
        head.add(getUniqueName(), entry);
    }

    @Override
    public void deserialize(final JsonObject head) {
        if(head.has(getUniqueName())) try {
            final JsonObject entry = head.get(getUniqueName()).getAsJsonObject();
            getProperties().forEach(option -> option.deserialize(entry));
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
        Iterator<IBlockProperty> it = getProperties().iterator();
        while(it.hasNext()) {
            IBlockProperty option = it.next();
            builder.append(option.toString());
            if(it.hasNext()) builder.append(", ");
        }
        builder.append("}");
        return builder.toString();
    }
}
