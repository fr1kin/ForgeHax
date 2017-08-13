package com.matt.forgehax.util.container.lists;

import com.matt.forgehax.util.container.ContainerList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.io.File;
import java.util.List;

public class ItemList extends ContainerList {
    public ItemList(String name, File file) {
        super(name, file);
    }

    public static List<ItemStack> getRegisteredItems() {
        NonNullList<ItemStack> itemList = NonNullList.create();
        for(Item item : Item.REGISTRY) {
            if(item != null) {
                //item.getSubItems(item, null, itemList);
            }
        }
        return itemList;
    }
}
