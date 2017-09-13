package com.matt.forgehax.util.gui.events;

/**
 * Created on 9/10/2017 by fr1kin
 */
public class GuiMouseEvent {
    public enum Type {
        PRESSED,
        DOWN,
        RELEASED,
    }

    private final Type type;

    private final int mouseCode;

    private final int mouseX;
    private final int mouseY;

    private final long timePreviouslyPressed;
    private final long timePressed;

    private final double delta;

    public GuiMouseEvent(Type type, int mouseCode, int mouseX, int mouseY, double delta, long timePreviouslyPressed, long timePressed) {
        this.type = type;
        this.mouseCode = mouseCode;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.delta = delta;
        this.timePressed = timePressed;
        this.timePreviouslyPressed = timePreviouslyPressed;
    }

    public Type getType() {
        return type;
    }

    public int getMouseCode() {
        return mouseCode;
    }

    public int getMouseX() {
        return mouseX;
    }

    public int getMouseY() {
        return mouseY;
    }

    public long getTimePreviouslyPressed() {
        return timePreviouslyPressed;
    }

    public long getTimePressed() {
        return timePressed;
    }

    public double getDelta() {
        return delta;
    }

    public boolean isWheelMoved() {
        return delta != 0;
    }
}
