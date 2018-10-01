package com.matt.forgehax.asm.events.replacementhooks;

import com.matt.forgehax.util.event.Cancelable;
import com.matt.forgehax.util.event.Event;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class RenderTooltipEvent extends Event implements Cancelable {
    protected final ItemStack stack;
    protected final List<String> lines;
    protected int x;
    protected int y;
    protected FontRenderer fr;

    public RenderTooltipEvent(@Nonnull ItemStack stack, @Nonnull List<String> lines, int x, int y, @Nonnull FontRenderer fr)
    {
        this.stack = stack;
        this.lines = Collections.unmodifiableList(lines); // Leave editing to ItemTooltipEvent
        this.x = x;
        this.y = y;
        this.fr = fr;
    }


    @Nonnull
    public ItemStack getStack()
    {
        return stack;
    }


    @Nonnull
    public List<String> getLines()
    {
        return lines;
    }


    public int getX()
    {
        return x;
    }


    public int getY()
    {
        return y;
    }


    @Nonnull
    public FontRenderer getFontRenderer()
    {
        return fr;
    }
}
