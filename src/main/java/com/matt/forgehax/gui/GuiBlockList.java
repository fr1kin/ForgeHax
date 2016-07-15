package com.matt.forgehax.gui;

import com.google.common.collect.Lists;
import com.matt.forgehax.gui.categories.BlockListCategory;
import com.matt.forgehax.util.SurfaceUtils;
import com.matt.forgehax.util.container.BlockList;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Map;

public class GuiBlockList extends GuiListExtended {
    private final List<IGuiListEntry> entries = Lists.newArrayList();

    public GuiBlockList(Minecraft mcIn, BlockListCategory.GuiBlockManager parent) {
        super(mcIn, parent.width, parent.height, 63, parent.height - 63, 16);
        for(Map.Entry<Block, Item> entry : BlockList.getRegisteredBlocks().entrySet()) {
            Block block = entry.getKey();
            Item item = entry.getValue();
            entries.add(new BlockEntry(block, item));
        }
        setHasListHeader(false, 0);
    }

    @Override
    public IGuiListEntry getListEntry(int index) {
        return entries.get(index);
    }

    @Override
    protected void overlayBackground(int startY, int endY, int startAlpha, int endAlpha) {
        super.overlayBackground(startY, endY, startAlpha, endAlpha);
    }

    @Override
    protected int getSize() {
        return entries.size();
    }

    public static class BlockEntry implements GuiListExtended.IGuiListEntry {
        private final Block block;
        private final Item item;
        private final ItemStack itemStack;

        public BlockEntry(Block block, Item item) {
            this.block = block;
            this.item = item;
            itemStack = new ItemStack(block);
        }

        @Override
        public void setSelected(int p_178011_1_, int p_178011_2_, int p_178011_3_) {

        }

        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected) {
            SurfaceUtils.drawItem(itemStack, x, y);
        }

        @Override
        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
            return false;
        }

        @Override
        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {

        }
    }
}
