package com.matt.forgehax.asm.reflection.type;

import com.google.common.collect.Lists;
import com.matt.forgehax.asm.helper.AsmStackLogger;
import joptsimple.internal.Strings;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created on 5/25/2017 by fr1kin
 */

public abstract class FastType<T> {
    protected static String[] removeInvalids(String[] names) {
        List<String> ns = Lists.newArrayList(names);
        ns.removeIf(Strings::isNullOrEmpty);
        return ns.toArray(new String[0]);
    }

    protected Class<?> insideClass;

    protected T type = null;

    protected boolean lookupFailed = false;
    protected AtomicBoolean printOnce = new AtomicBoolean(false);

    public FastType(Class<?> insideClass) {
        Objects.requireNonNull(insideClass);
        this.insideClass = insideClass;
    }

    public boolean isError() {
        return printOnce.get();
    }

    protected boolean attemptLookup() throws Exception {
        if(!lookupFailed) {
            if (type == null) {
                type = lookup();
                lookupFailed = (type == null);
            }
            return !lookupFailed;
        } else return true; // previous attempt failed, trying again wont work
    }

    /**
     * Reflection lookup
     */
    protected abstract T lookup() throws Exception;
}
