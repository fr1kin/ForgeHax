package com.matt.forgehax.util.gui;

/**
 * Created on 9/14/2017 by fr1kin
 */
public class GuiHelper {
    public static boolean isInArea(double x, double y, double topX, double topY, double bottomX, double bottomY) {
        return x > topX
                && x < bottomX
                && y > topY
                && y < bottomY;
    }
}
