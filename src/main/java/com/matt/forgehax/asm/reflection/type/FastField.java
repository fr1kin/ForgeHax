package com.matt.forgehax.asm.reflection.type;

import com.matt.forgehax.asm.helper.AsmStackLogger;
import joptsimple.internal.Strings;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;

/**
 * Created on 5/25/2017 by fr1kin
 */
public class FastField<V> extends FastType<Field> {
    public FastField(Class<?> insideClass, String[] names) {
        super(insideClass, names);
    }

    public <E> V get(E instance, V defaultValue) {
        try {
            if(attemptLookup()) return (V)type.get(instance);
        } catch (Exception e) {
            if(printOnce.compareAndSet(false, true))
                AsmStackLogger.printStackTrace(e);
        }
        return defaultValue;
    }

    public <E> V get(E instance) {
        return get(instance, null);
    }

    public <E> boolean set(E instance, V to) {
        try {
            if(attemptLookup()) {
                type.set(instance, to);
                return true;
            }
        } catch (Exception e) {
            if(printOnce.compareAndSet(false, true))
                AsmStackLogger.printStackTrace(e);
        }
        return false; // failed to set
    }

    @Override
    protected Field lookup() throws Exception {
        return ReflectionHelper.findField(insideClass, names);
    }
}
