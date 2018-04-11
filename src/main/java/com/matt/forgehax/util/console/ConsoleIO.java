package com.matt.forgehax.util.console;

import com.matt.forgehax.Globals;
import com.matt.forgehax.Helper;
import com.matt.forgehax.mods.services.MainMenuGuiService.CommandInputGui;
import joptsimple.internal.Strings;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created on 6/10/2017 by fr1kin
 */

// TODO: fix memory leak
public class ConsoleIO implements Globals {
    public static final Style HEADING = new Style().setColor(TextFormatting.GRAY).setItalic(true);

    private static final ThreadLocal<AtomicInteger> INDENTATION = new ThreadLocal<>();
    private static final int MIN_INDENT = 1;

    private static AtomicInteger getOrCreate() {
        AtomicInteger count = INDENTATION.get();
        if(count == null) {
            count = new AtomicInteger(MIN_INDENT);
            INDENTATION.set(count);
        }
        return count;
    }

    public static void start() {
        getOrCreate().set(MIN_INDENT);
    }

    public static void write(String msg, Style style) {
        String tab = Strings.repeat('>', Math.max(getOrCreate().get(), MIN_INDENT)) + " ";
        if(style == null)
            Helper.printMessageNaked(tab, msg); //TODO: use a non-chat console
        else
            Helper.printMessageNaked(tab, msg, style); //TODO: use a non-chat console
    }

    public static void write(String msg) {
        write(msg, null);
    }

    public static void incrementIndent() {
        getOrCreate().incrementAndGet();
    }

    public static void decrementIndent() {
        getOrCreate().decrementAndGet();
    }

    public static int getIndents() {
        return getOrCreate().get();
    }

    public static void setIndents(int indents) {
        getOrCreate().set(indents);
    }

    public static void finished() {
        INDENTATION.remove();
    }
}
