package com.matt.forgehax.gui;

import com.google.gson.JsonElement;
import com.matt.forgehax.util.SurfaceUtils;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.container.PlayerList;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.client.GuiScrollingList;

import java.util.Collection;
import java.util.Map;

public class GuiPlayerList extends GuiScrollingList {
    private GuiScreen parent;
    private PlayerList selectedList;

    private int selectedIndex = -1;

    public GuiPlayerList(GuiScreen parent, int x, int y, int width, int height, int slotHeight, int screenWidth, int screenHeight, PlayerList selectedList) {
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
        if(selectedList != null) {
            this.selectedList = selectedList;
            selectedIndex = selectedList.entrySet().size() - 1;
        }
    }

    @Override
    protected int getSize() {
        return selectedList != null ? selectedList.entrySet().size() : 0;
    }

    @Override
    protected void elementClicked(int index, boolean doubleClick) {
        selectedIndex = index;
    }

    @Override
    protected boolean isSelected(int index) {
        return selectedIndex == index;
    }

    @Override
    protected void drawBackground() {
        int scale = 2;
        SurfaceUtils.drawRect(left - scale, top - scale, listWidth + 2 * scale, listHeight + 2 * scale, Utils.Colors.BLACK);
    }

    @Override
    protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
        int x = entryRight - listWidth + slotHeight;
        int y = slotTop;

        if(selectedList == null)
            return;

        int index = 0;
        Map.Entry<String, JsonElement> selected = null;
        for(Map.Entry<String, JsonElement> entry : selectedList.entrySet()) {
            if(index == selectedIndex) {
                selected = entry;
                break;
            }
            index++;
        }

        if(selected == null)
            return;

        
    }
}
