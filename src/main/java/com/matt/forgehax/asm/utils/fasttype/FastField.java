package com.matt.forgehax.asm.utils.fasttype;

import com.matt.forgehax.asm.utils.ASMStackLogger;
import com.matt.forgehax.asm.utils.environment.State;
import com.matt.forgehax.asm.utils.name.IName;
import joptsimple.internal.Strings;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;

/**
 * Created on 5/25/2017 by fr1kin
 */
public class FastField<V> extends FastType<Field> {
    public FastField(Class<?> insideClass, IName<String> name) {
        super(insideClass, name);
    }

    public <E> V get(E instance, V defaultValue) {
        try {
            if(attemptLookup()) return (V)type.get(instance);
        } catch (Exception e) {
            if(printOnce.compareAndSet(false, true))
                ASMStackLogger.printStackTrace(e);
        }
        return defaultValue;
    }

    public <E> V get(E instance) {
        return get(instance, null);
    }

    public V getStatic(V defaultValue) {
        return get(null, defaultValue);
    }

    public V getStatic() {
        return get(null);
    }

    public <E> boolean set(E instance, V to) {
        try {
            if(attemptLookup()) {
                type.set(instance, to);
                return true;
            }
        } catch (Exception e) {
            if(printOnce.compareAndSet(false, true))
                ASMStackLogger.printStackTrace(e);
        }
        return false; // failed to set
    }

    public boolean setStatic(V to) {
        return set(null, to);
    }

    @Override
    protected Field lookup() throws Exception {
        for(State state : State.values()) {
            String n = name.getByState(state);
            if(!Strings.isNullOrEmpty(n)) try {
                    Field f = insideClass.getDeclaredField(n);
                    f.setAccessible(true);
                    return f;
            } catch (Exception e) {
                ;
            }
        }
        return null;
    }
}
