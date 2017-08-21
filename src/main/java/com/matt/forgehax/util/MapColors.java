package com.matt.forgehax.util;

import net.minecraft.block.material.MapColor;


/**
 * Created by Babbaj on 8/19/2017.
 */

public class MapColors {

    public static final int[] COLOR_LIST = new int[208]; // list of all possible colors used on maps, 4 for each base color
    public static final int[] BASE_COLORS = new int[52]; // list of base colors from net.minecraft.block.material.MapColor


    static {
        for (int i = 0; i < BASE_COLORS.length; i++) { // get integer color values from MapColor object list
            BASE_COLORS[i] = MapColor.COLORS[i].colorValue;
        }

        for (int i = 0; i < BASE_COLORS.length; i++) { // generates full list of colors from the list of base colors
            int[] rgb = Utils.toRGBAArray(BASE_COLORS[i]);
            COLOR_LIST[i * 4 + 0] = Utils.toRGBA((rgb[0]*180)/255, (rgb[1]*180)/255, (rgb[2]*180)/255, 0);
            COLOR_LIST[i * 4 + 1] = Utils.toRGBA((rgb[0]*220)/255, (rgb[1]*220)/255, (rgb[2]*220)/255, 0);
            COLOR_LIST[i * 4 + 2] = BASE_COLORS[i];
            COLOR_LIST[i * 4 + 3] = Utils.toRGBA((rgb[0]*135)/255, (rgb[1]*135)/255, (rgb[2]*135)/255, 0);
        }
    }
}
