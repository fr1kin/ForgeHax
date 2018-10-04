package com.matt.forgehax.util.gui.test;

import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.draw.Fonts;
import com.matt.forgehax.util.gui.mc.GuiParentScreen;
import com.matt.forgehax.util.gui.mcgui.MButton;
import com.matt.forgehax.util.gui.mcgui.MCheckbox;
import com.matt.forgehax.util.gui.mcgui.MLabel;
import com.matt.forgehax.util.gui.mcgui.MWindow;

/** Created on 9/12/2017 by fr1kin */
public class GuiTestMain extends GuiParentScreen {

  public GuiTestMain() {
    setFontRenderer(Fonts.ARIAL);

    MWindow window = new MWindow();
    window.setParent(this);

    window.setPos(50, 50);
    window.setSize(100, 100);

    window.setBackgroundColor(Utils.toRGBA(255, 0, 0, 150));

    window.setFontColor(Utils.Colors.WHITE);
    window.setFontRenderer(Fonts.ARIAL);

    String[] labels = new String[] {"Hello", "These", "Are", "Labels"};

    double h = 0;
    for (int i = 0; i < labels.length; i++) {
      MLabel label = new MLabel();
      label.setParent(window);
      label.setText(labels[i]);
      label.setFontColor(Utils.Colors.WHITE);
      label.setPos(1, MWindow.BAR_HEIGHT + (i * label.getHeight() + 2));
      h = label.getHeight();
    }

    MCheckbox checkbox = new MCheckbox();
    checkbox.setParent(window);
    checkbox.setText("Hello");
    checkbox.setHoverText("This is a checkbox");
    checkbox.setPos(1, MWindow.BAR_HEIGHT + (labels.length * h + 2));

    // panel2
    MWindow window2 = new MWindow();
    window2.setParent(this);

    window2.setPos(50, 50);
    window2.setSize(100, 100);

    window2.setBackgroundColor(Utils.toRGBA(0, 255, 0, 150));

    MButton button1 = new MButton();
    button1.setParent(window2);
    button1.setText("hello");
    button1.setSize(75, 25);
    button1.setPos(1, MWindow.BAR_HEIGHT + 1);

    // panel3
    MWindow window3 = new MWindow();
    window3.setParent(this);

    window3.setPos(50, 50);
    window3.setSize(100, 100);

    window3.setBackgroundColor(Utils.toRGBA(0, 0, 255, 150));

    // focus
    window.requestFocus();
  }
}
