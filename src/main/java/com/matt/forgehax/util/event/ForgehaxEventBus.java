package com.matt.forgehax.util.event;


import com.matt.forgehax.util.event.Event;

public class ForgehaxEventBus implements EventBus {

    private final com.google.common.eventbus.EventBus GUAVA_EVENT_BUS = new com.google.common.eventbus.EventBus("ForgehaxEventBus");

    @Override
    public boolean post(Object event) {
        GUAVA_EVENT_BUS.post(event);
        if (event instanceof Event && event instanceof Cancelable) {
            return ((Event)event).isCanceled();
        }
        if (event instanceof net.minecraftforge.fml.common.eventhandler.Event) {
            return ((net.minecraftforge.fml.common.eventhandler.Event)event).isCanceled(); // TODO: remove this
        }
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
