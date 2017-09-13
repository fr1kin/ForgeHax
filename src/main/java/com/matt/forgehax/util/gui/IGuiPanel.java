package com.matt.forgehax.util.gui;

/**
 * Created on 9/9/2017 by fr1kin
 */
public interface IGuiPanel extends IGuiParent {
    boolean isCollapsed();
    void setCollapsed(boolean collapsed);

    int getMaxRows();
    void setMaxRows(int rows);

    int getMaxColumns();
    void setMaxColumns(int columns);

    boolean isVerticalScrolling();
    void setVerticalScrolling(boolean scrolling);

    boolean isHorizontalScrolling();
    void setHorizontalScrolling(boolean scrolling);
}
