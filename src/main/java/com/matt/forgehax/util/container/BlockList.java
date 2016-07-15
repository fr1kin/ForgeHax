package com.matt.forgehax.util.container;

import com.google.common.collect.BiMap;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class BlockList {
    public static Map<Block, Item> getRegisteredBlocks() {
        return GameData.getBlockItemMap();
    }
}
