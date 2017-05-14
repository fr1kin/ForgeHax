package com.matt.forgehax.util.blocks;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.matt.forgehax.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

import java.util.Collection;
import java.util.Objects;

/**
 * Created on 5/13/2017 by fr1kin
 */
public class BlockEntry {
    private final String fullName;

    private Block block = null;
    private String name = "";

    private boolean isMetadata = false;
    private int metadataId = 0;

    private int colorBuffer = -1;

    public BlockEntry(String name) {
        this.fullName = name;
        String[] sub = name.split("::");
        this.name = sub[0];
        this.block = Block.getBlockFromName(this.name);
        if(sub.length > 1) {
            isMetadata = true;
            metadataId = Integer.parseInt(sub[1]);
        }
    }

    public boolean matches(IBlockState state) {
        return Objects.equals(block, state.getBlock()) && (!isMetadata || state.getBlock().getMetaFromState(state) == metadataId);
    }

    public String getFullName() {
        return fullName;
    }

    public Block getBlock() {
        return block;
    }

    public int getColorBuffer() {
        return colorBuffer;
    }

    public void read(JsonObject node) {
        int r = 255, g = 255, b = 255, a = 255;
        if(node.has("r")) r = node.get("r").getAsInt();
        if(node.has("g")) g = node.get("g").getAsInt();
        if(node.has("b")) b = node.get("b").getAsInt();
        if(node.has("a")) a = node.get("a").getAsInt();
        this.colorBuffer = Utils.toRGBA(r, g, b, a);
    }

    public void write(JsonObject node) {
        if(colorBuffer == -1) return;
        int r = (colorBuffer >> 16 & 255);
        int g = (colorBuffer >> 8 & 255);
        int b = (colorBuffer & 255);
        int a = (colorBuffer >> 24 & 255);
        node.addProperty("r", r);
        node.addProperty("g", g);
        node.addProperty("b", b);
        node.addProperty("a", a);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BlockEntry && Objects.equals(fullName, ((BlockEntry) obj).fullName);
    }
}
