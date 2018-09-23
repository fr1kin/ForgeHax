package com.matt.forgehax.util.event;


import net.minecraftforge.fml.common.eventhandler.Event;

public class ForgehaxEventBus implements EventBus {

    public static final EventBus EVENT_BUS = new ForgehaxEventBus();

    private final com.google.common.eventbus.EventBus GUAVA_EVENT_BUS = new com.google.common.eventbus.EventBus("ForgehaxEventBus");

    @Override
    public boolean post(Object event) {
        GUAVA_EVENT_BUS.post(event);
        if (event instanceof Cancellable) return ((Cancellable)event).isCancelled();
        if (event instanceof Event) return ((Event)event).isCanceled(); // TODO: remove this
        return false;
    }

    @Override
    public void register(Object object) {
        GUAVA_EVENT_BUS.register(object);
    }

    @Override
    public void unregister(Object object) {
        GUAVA_EVENT_BUS.unregister(object);
    }
}
