package com.matt.forgehax.util.gui.test;

import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.draw.Fonts;
import com.matt.forgehax.util.gui.base.GuiLabel;
import com.matt.forgehax.util.gui.base.GuiPanel;

/**
 * Created on 9/12/2017 by fr1kin
 */
public class GuiTestMain extends GuiPanel {
    @Override
    public void init(double screenWidth, double screenHeight) {
        super.init(screenWidth, screenHeight);

        removeAllChildren();

        setPos(50, 50);
        setSize(100, 100);

        setFontColor(0, 0, 0, 150);
        setFontRenderer(Fonts.ARIAL);

        String[] labels = new String[] {"Hello", "These", "Are", "Labels"};

        for(int i = 0; i < labels.length; i++) {
            GuiLabel label = new GuiLabel();
            label.setParent(this);
            label.setText(labels[i]);
            label.setFontColor(Utils.Colors.WHITE);
            label.setPos(1, 1);
        }

        // focus
        focus();
    }
}
