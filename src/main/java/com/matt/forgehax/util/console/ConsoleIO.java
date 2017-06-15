package com.matt.forgehax.util.console;

import com.matt.forgehax.Globals;
import com.matt.forgehax.Helper;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;

/**
 * Created on 6/10/2017 by fr1kin
 */
public class ConsoleIO implements Globals {
    public static void writeHead(String msg) {
        Helper.printMessageNaked("> ", msg, new Style().setColor(TextFormatting.GRAY).setItalic(true)); //TODO: use a non-chat console
    }

    public static void write(String msg) {
        Helper.printMessageNaked(">> ", msg); //TODO: use a non-chat console
    }
}
