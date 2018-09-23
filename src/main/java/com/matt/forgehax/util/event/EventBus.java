package com.matt.forgehax.util.event;

public interface EventBus {
    // return true if cancelled
    boolean post(Object event);

    void register(Object object);

    void unregister(Object object);
}
