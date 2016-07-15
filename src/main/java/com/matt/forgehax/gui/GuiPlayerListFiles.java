package com.matt.forgehax.gui;

import com.google.common.collect.Lists;
import com.matt.forgehax.gui.categories.PlayerListCategory;
import com.matt.forgehax.util.SurfaceUtils;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.container.PlayerList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.client.GuiScrollingList;
import net.minecraftforge.fml.client.config.GuiConfig;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class GuiPlayerListFiles extends GuiScrollingList {
    private final GuiScreen parent;
    private final List<PlayerList> elements = Lists.newArrayList();

    private int selectedIndex = -1;

    public GuiPlayerListFiles(GuiScreen parent, int x, int y, int width, int height, int slotHeight, int screenWidth, int screenHeight, Collection<PlayerList> playerLists) {
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
        this.parent = parent;
        for(PlayerList list : playerLists)
            elements.add(list);
        // select last
        selectedIndex = elements.size() - 1;
    }

    public PlayerList getSelectedPlayerList() {
        if(selectedIndex > -1 &&
                selectedIndex < elements.size())
            return elements.get(selectedIndex);
        else return null;
    }

    public void setParentSelectedPlayerList() {
        if(parent != null &&
                selectedIndex > -1 &&
                selectedIndex < elements.size())
            ((PlayerListCategory.GuiPlayerManager)(parent)).onSelectPlayerListFile(elements.get(selectedIndex));
    }

    @Override
    protected int getSize() {
        return elements.size();
    }

    @Override
    protected void elementClicked(int index, boolean doubleClick) {
        selectedIndex = index;
        setParentSelectedPlayerList();
    }

    @Override
    protected boolean isSelected(int index) {
        return selectedIndex == index;
    }

    @Override
    protected void drawBackground() {
        int scale = 2;
        SurfaceUtils.drawRect(left - scale, top - scale, listWidth + 2*scale, listHeight + 2*scale, Utils.Colors.BLACK);
    }

    @Override
    protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
        int x = entryRight - listWidth + slotHeight;
        int y = slotTop;

        PlayerList list = elements.get(slotIdx);

        SurfaceUtils.drawTextShadow(list.getName(), x, y, Utils.Colors.WHITE);
    }
}
