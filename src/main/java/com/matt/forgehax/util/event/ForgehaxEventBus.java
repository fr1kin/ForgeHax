package com.matt.forgehax.util.event;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ForgehaxEventBus implements EventBus {

    private final com.google.common.eventbus.EventBus GUAVA_EVENT_BUS = new com.google.common.eventbus.EventBus("ForgehaxEventBus");

    public ForgehaxEventBus() {
        // use immediate dispatcher rather than threaded dispatcher
        try {
            Field dispatcherField = com.google.common.eventbus.EventBus.class.getDeclaredField("dispatcher");
            dispatcherField.setAccessible(true);
            Class<?> dispatcherClass = Class.forName("com.google.common.eventbus.Dispatcher");
            Method immediate = dispatcherClass.getDeclaredMethod("immediate");
            immediate.setAccessible(true);
            final Object dispatcher = immediate.invoke(null);
            dispatcherField.set(GUAVA_EVENT_BUS, dispatcher);
        } catch (ReflectiveOperationException ex) {
            throw new Error(ex);
        }
    }

    @Override
    public boolean post(Object event) {
        GUAVA_EVENT_BUS.post(event);
        if (event instanceof Event && event instanceof Cancelable) {
            return ((Event)event).isCanceled();
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
