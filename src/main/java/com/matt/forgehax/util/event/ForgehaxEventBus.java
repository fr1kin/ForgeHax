package com.matt.forgehax.util.event;


import net.minecraftforge.fml.common.eventhandler.Event;

public class ForgehaxEventBus implements EventBus {

    private final com.google.common.eventbus.EventBus GUAVA_EVENT_BUS = new com.google.common.eventbus.EventBus("ForgehaxEventBus");

    @Override
    public boolean post(Object event) {
        GUAVA_EVENT_BUS.post(event);
        if (event instanceof Cancelable) return ((Cancelable)event).isCanceled();
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
