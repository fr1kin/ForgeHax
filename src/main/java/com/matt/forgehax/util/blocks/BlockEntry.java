package com.matt.forgehax.util.blocks;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.matt.forgehax.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.Objects;

/**
 * Created on 5/13/2017 by fr1kin
 */
public class BlockEntry {
    private final Block block;
    private final ResourceLocation resourceLocation;

    private boolean isMetadata = false;
    private int metadataId = -1;

    private int colorBuffer = Utils.Colors.WHITE;

    public BlockEntry(ResourceLocation resourceLocation) throws BlockDoesNotExistException {
        this.resourceLocation = resourceLocation;
        this.block = Block.getBlockFromName(resourceLocation.toString());
        if(this.block == null || block.equals(Blocks.AIR)) throw new BlockDoesNotExistException("block '" + resourceLocation.toString() + "' does not exist");
    }

    public BlockEntry(String name) throws BlockDoesNotExistException {
        this(new ResourceLocation(name));
    }

    public BlockEntry(String domain, String name) throws BlockDoesNotExistException {
        this(new ResourceLocation(domain, name));
    }

    public BlockEntry(int id) throws BlockDoesNotExistException {
        this.block = Block.getBlockById(id);
        if(this.block == null || block.equals(Blocks.AIR)) throw new BlockDoesNotExistException("block id'" + String.valueOf(id) + "' does not exist");
        this.resourceLocation = this.block.getRegistryName();
    }

    public boolean matches(IBlockState state) {
        return Objects.equals(block, state.getBlock()) && (!isMetadata || state.getBlock().getMetaFromState(state) == metadataId);
    }

    public String getName() {
        return resourceLocation.toString();
    }

    public Block getBlock() {
        return block;
    }

    public int getColorBuffer() {
        return colorBuffer;
    }

    public void setColorBuffer(int r, int g, int b, int a) {
        this.colorBuffer = Utils.toRGBA(r, g, b, a);
    }

    public boolean isMetadata() {
        return isMetadata;
    }

    public int getMetadataId() {
        return metadataId;
    }

    public void setMetadataId(int metadataId) {
        if(metadataId > -1) {
            this.isMetadata = true;
            this.metadataId = metadataId;
        }
    }

    public void read(JsonObject node) {
        int r = 255, g = 255, b = 255, a = 255;
        if(node.has("r")) r = node.get("r").getAsInt();
        if(node.has("g")) g = node.get("g").getAsInt();
        if(node.has("b")) b = node.get("b").getAsInt();
        if(node.has("a")) a = node.get("a").getAsInt();
        if(node.has("m")) setMetadataId(node.get("m").getAsInt());
        this.colorBuffer = Utils.toRGBA(r, g, b, a);
    }

    public void write(JsonObject node) {
        int r = (colorBuffer >> 16 & 255);
        int g = (colorBuffer >> 8 & 255);
        int b = (colorBuffer & 255);
        int a = (colorBuffer >> 24 & 255);
        node.addProperty("r", r);
        node.addProperty("g", g);
        node.addProperty("b", b);
        node.addProperty("a", a);
        if(isMetadata) node.addProperty("m", metadataId);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BlockEntry &&
                Objects.equals(getBlock(), ((BlockEntry) obj).getBlock()) &&
                (!isMetadata || this.metadataId == ((BlockEntry) obj).metadataId);
    }
}
