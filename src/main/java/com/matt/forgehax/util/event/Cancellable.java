package com.matt.forgehax.util.event;

public abstract class Cancellable {
    private boolean cancelled;

    public final void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public final boolean isCancelled() {
        return cancelled;
    }
}
