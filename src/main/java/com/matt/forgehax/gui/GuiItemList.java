package com.matt.forgehax.gui;

import com.google.common.collect.Lists;
import com.matt.forgehax.util.container.lists.ItemList;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.GuiScrollingList;

import java.util.List;

public class GuiItemList extends GuiScrollingList {
    private final ItemList itemList;

    private final List<List<ItemStack>> ITEMS = Lists.newArrayList();

    public GuiItemList(GuiScreen parent, int x, int y, int width, int height, int slotHeight, int screenWidth, int screenHeight, List<ItemStack> allItems, ItemList selectedList) {
        super(
                parent.mc,
                width,
                height,
                y,
                y + height,
                x,
                slotHeight,
                screenWidth,
                screenHeight
        );
        List<ItemStack> current = null;
        for(int i = 0, pos = 0; i < allItems.size(); i++) {
            if(pos < ITEMS.size()) {

            }
        }
        itemList = selectedList;
    }

    @Override
    protected int getSize() {
        return itemList.size();
    }

    @Override
    protected void elementClicked(int index, boolean doubleClick) {

    }

    @Override
    protected boolean isSelected(int index) {
        return false;
    }

    @Override
    protected void drawBackground() {

    }

    @Override
    protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {

    }
}
