package com.matt.forgehax.asm.utils.debug;

import com.google.common.collect.Lists;
import com.matt.forgehax.asm.events.listeners.ListenerHook;
import com.matt.forgehax.asm.utils.MultiBoolean;
import com.matt.forgehax.asm.utils.asmtype.ASMMethod;
import com.matt.forgehax.util.Immutables;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created on 2/11/2018 by fr1kin
 */
public class HookReporter {
    private final Method method;

    private final List<ASMMethod> hookedMethods;
    private final List<Class<?>> eventClasses;

    private boolean responding = false;
    private MultiBoolean activator = new MultiBoolean();

    private HookReporter(Method method,
                         Collection<ASMMethod> hookedMethods,
                         Collection<Class<?>> eventClasses,
                         boolean startDisabled) throws NullPointerException {
        Objects.requireNonNull(method);

        this.method = method;
        this.hookedMethods = Immutables.copyToList(hookedMethods);
        this.eventClasses = Immutables.copyToList(eventClasses);

        if(!startDisabled) enable();
    }

    /**
     * Gets the hook method this object represents
     * @return hook method
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Gets the methods that are hooked in order to call our hook
     * @return immutable list of hooked methods
     */
    public List<ASMMethod> getHookedMethods() {
        return hookedMethods;
    }

    /**
     * Gets all the event classes that are created by this hook
     * @return
     */
    public List<Class<?>> getEventClasses() {
        return eventClasses;
    }

    public List<Class<? extends Event>> getForgeEventClasses() {
        return eventClasses.stream()
                .filter(Event.class::isAssignableFrom)
                .map(clazz -> (Class<? extends Event>)clazz)
                .collect(Collectors.toList());
    }

    public List<Class<? extends ListenerHook>> getListenerEventClasses() {
        return eventClasses.stream()
                .filter(ListenerHook.class::isAssignableFrom)
                .map(clazz -> (Class<? extends ListenerHook>)clazz)
                .collect(Collectors.toList());
    }

    /**
     * Check if the hook has been called yet
     * @return true if the hook has been invoked
     */
    public boolean isResponding() {
        return responding;
    }

    /**
     * Reports this hook as functional
     * Should only call this within the hook
     * @return if the hook is active
     */
    public boolean reportHook() {
        responding = true;
        return activator.isEnabled();
    }

    /**
     * Gets the activator object to enable and disable this hook
     * @return activator instance
     */
    public MultiBoolean getActivator() {
        return activator;
    }

    /**
     * Enables the hook
     */
    public void enable() {
        activator.enable("root");
    }

    /**
     * Force disables the hook
     */
    public void disable() {
        activator.forceDisable();
    }

    /**
     * If the hook is currently not being used in anyway
     * @return true if not being used
     */
    public boolean isDeprecatedHook() {
        return method.isAnnotationPresent(Deprecated.class);
    }

    public static class Builder {
        public static Builder of() {
            return new Builder();
        }

        private Builder() {}

        private Method method;
        private List<ASMMethod> hookedMethods = Lists.newArrayList();
        private List<Class<?>> eventClasses = Lists.newArrayList();
        private boolean startDisabled = false;
        private Class<?> parentClass;
        private Consumer<HookReporter> finalizeBy;

        /**
         * Set the parent class.
         * Only a convenience method for hook(string)
         * @param parentClass parent class containing the method
         * @return this
         */
        public Builder parentClass(Class<?> parentClass) {
            this.parentClass = parentClass;
            return this;
        }

        /**
         * Set the hook method this object will represent
         * @param method hook method
         * @return this
         */
        public Builder hook(Method method) {
            this.method = method;
            return this;
        }
        public Builder hook(Class<?> parentClass, final String methodName) throws InvalidMethodException {
            Objects.requireNonNull(parentClass);
            Objects.requireNonNull(methodName);

            List<Method> results =  Arrays.stream(parentClass.getDeclaredMethods())
                    .filter(m -> methodName.equals(m.getName()))
                    .collect(Collectors.toList());

            if(results.size() == 1)
                return hook(results.get(0));
            else if(results.size() > 1)
                throw new InvalidMethodException("Found two methods with the same name");
            else
                throw new InvalidMethodException("No such method found");
        }
        public Builder hook(final String methodName) throws InvalidMethodException {
            Objects.requireNonNull(parentClass, "this method requires this.parentClass be set");
            return hook(parentClass, methodName);
        }

        public Builder forgeEvent(Class<? extends Event> clazz) {
            eventClasses.add(clazz);
            return this;
        }
        public Builder listenerEvent(Class<? extends ListenerHook> clazz) {
            eventClasses.add(clazz);
            return this;
        }

        /**
         * Hooked method that this hook depends on.
         * @param method hooked method
         * @return this
         */
        public Builder dependsOn(ASMMethod method) {
            hookedMethods.add(method);
            return this;
        }

        public Builder startOn() {
            startDisabled = false;
            return this;
        }

        public Builder startOff() {
            startDisabled = true;
            return this;
        }

        /**
         * Method to call when build() is finally called
         * @param finalizeBy final function to execute
         * @return this
         */
        public Builder finalizeBy(Consumer<HookReporter> finalizeBy) {
            this.finalizeBy = finalizeBy;
            return this;
        }

        public HookReporter build() {
            final HookReporter hp = new HookReporter(method, hookedMethods, eventClasses, startDisabled);
            if(finalizeBy != null) finalizeBy.accept(hp);
            return hp;
        }
    }

    public static class InvalidMethodException extends RuntimeException {
        public InvalidMethodException(String message) {
            super(message);
        }
    }
}
