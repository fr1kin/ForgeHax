package com.matt.forgehax.util.container.lists;

import com.google.common.collect.BiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.matt.forgehax.util.container.ContainerList;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ItemList extends ContainerList {
    public ItemList(String name, File file) {
        super(name, file);
    }

    public static List<ItemStack> getRegisteredItems() {
        NonNullList<ItemStack> itemList = NonNullList.func_191196_a();
        for(Item item : Item.REGISTRY) {
            if(item != null) {
                item.getSubItems(item, null, itemList);
            }
        }
        return itemList;
    }
}
