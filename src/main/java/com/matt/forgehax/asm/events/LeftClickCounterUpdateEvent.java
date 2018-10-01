package com.matt.forgehax.asm.events;

import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.asm.utils.ReflectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.Event;

public class LeftClickCounterUpdateEvent extends Event {
    private final int currentValue;
    private int value;

    public LeftClickCounterUpdateEvent(int value) {
        this.currentValue = FastReflection.Fields.Minecraft_leftClickCounter.get(Minecraft.getMinecraft());
        this.value = value;
    }

    public int getCurrentValue() {
        return currentValue;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
