package com.matt.forgehax.gui;

import com.google.common.collect.Lists;
import com.matt.forgehax.gui.categories.IGuiCategory;
import com.matt.forgehax.util.draw.SurfaceUtils;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.container.ContainerList;
import jline.internal.Nullable;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.client.GuiScrollingList;

import java.util.Collection;
import java.util.List;

public class GuiCategoryFileList extends GuiScrollingList {
    private final GuiScreen parent;
    private final IGuiCategory callback;
    private final List<Object> elements = Lists.newArrayList();

    private int selectedIndex = -1;

    public GuiCategoryFileList(GuiScreen parent, int x, int y, int width, int height, int slotHeight, int screenWidth, int screenHeight, Collection<?> listCollection) {
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
        if(parent instanceof IGuiCategory)
            this.callback = (IGuiCategory)parent;
        else
            this.callback = null;
        // copy list
        for(Object list : listCollection)
            elements.add(list);
    }

    public boolean isValidIndex(int index) {
        return index > -1 &&
                index < elements.size();
    }

    public void setSelectedIndex(int index) {
        setSelected(elements.get(index));
    }

    public void setSelected(@Nullable Object selected) {
        if(selected != null) {
            int index = elements.indexOf(selected);
            if (callback != null &&
                    isValidIndex(index)) {
                selectedIndex = index;
                callback.onSelectedFileFromList(selected);
            }
        } else {
            selectedIndex = -1;
        }
    }

    @Override
    protected int getSize() {
        return elements.size();
    }

    @Override
    protected void elementClicked(int index, boolean doubleClick) {
        setSelectedIndex(index);
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
        int x = entryRight - listWidth + slotBuffer + 2;
        int y = slotTop;
        Object atIndex = elements.get(slotIdx);
        if(atIndex instanceof ContainerList) {
            ContainerList list = (ContainerList)atIndex;
            SurfaceUtils.drawTextShadow(list.getName(), x, y, Utils.Colors.WHITE);
        }
    }
}
