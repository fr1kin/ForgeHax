package com.matt.forgehax.asm.events;

import net.minecraftforge.fml.common.eventhandler.Event;

public class OnSendClickBlockToControllerEvent extends Event {
    private boolean clicked;

    public OnSendClickBlockToControllerEvent(boolean clicked) {
        this.clicked = clicked;
    }

    public boolean isClicked() {
        return clicked;
    }

    public void setClicked(boolean clicked) {
        this.clicked = clicked;
    }
}
