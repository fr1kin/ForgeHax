package com.matt.forgehax.util.event;

public abstract class Cancelable {
    private boolean cancelled;

    public final void setCanceled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public final boolean isCanceled() {
        return cancelled;
    }
}
