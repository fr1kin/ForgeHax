package com.matt.forgehax.util.gui.test;

import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.draw.Fonts;
import com.matt.forgehax.util.gui.mc.GuiParentScreen;
import com.matt.forgehax.util.gui.mcgui.MCheckbox;
import com.matt.forgehax.util.gui.mcgui.MLabel;
import com.matt.forgehax.util.gui.mcgui.MPanel;

/**
 * Created on 9/12/2017 by fr1kin
 */
public class GuiTestMain extends GuiParentScreen {

    public GuiTestMain() {
        super();

        MPanel panel = new MPanel();
        panel.setParent(this);

        panel.setPos(50, 50);
        panel.setSize(100, 100);

        panel.setBackgroundColor(Utils.toRGBA(255, 0, 0, 150));

        panel.setFontColor(Utils.Colors.WHITE);
        panel.setFontRenderer(Fonts.ARIAL);

        String[] labels = new String[] {"Hello", "These", "Are", "Labels"};

        double h = 0;
        for(int i = 0; i < labels.length; i++) {
            MLabel label = new MLabel();
            label.setParent(panel);
            label.setText(labels[i]);
            label.setFontColor(Utils.Colors.WHITE);
            label.setPos(1, (i * label.getHeight() + 2));
            h = label.getHeight();
        }

        MCheckbox checkbox = new MCheckbox();
        checkbox.setParent(panel);
        checkbox.setText("Hello");
        checkbox.setHoverText("This is a checkbox");
        checkbox.setPos(1, (labels.length * h + 2));

        // panel2
        MPanel panel2 = new MPanel();
        panel2.setParent(this);

        panel2.setPos(75, 75);
        panel2.setSize(100, 100);

        panel2.setBackgroundColor(Utils.toRGBA(0, 255, 0, 150));

        // panel3
        MPanel panel3 = new MPanel();
        panel3.setParent(this);

        panel3.setPos(75, 25);
        panel3.setSize(100, 100);

        panel3.setBackgroundColor(Utils.toRGBA(0, 0, 255, 150));

        // focus
        panel.requestFocus();
    }
}
