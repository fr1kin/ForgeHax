package com.matt.forgehax.util.event;

public class Event {
    private boolean canceled;

    public final void setCanceled(boolean b) {
        if (!(this instanceof Cancelable)) throw new UnsupportedOperationException("Event can not be canceled");
        this.canceled = b;
    }

    public final boolean isCanceled() {
        return canceled;
    }
}
