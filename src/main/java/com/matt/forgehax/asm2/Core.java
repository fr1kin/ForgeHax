package com.matt.forgehax.asm2;

import com.google.common.collect.Lists;
import com.matt.forgehax.asm2.util.ObfuscationHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;

/**
 * Created on 1/26/2017 by fr1kin
 */
public class Core {
    private static final Logger LOGGER = LogManager.getLogger("ForgeHaxAsm");

    private static boolean obfuscated = true;

    public static Logger getLogger() {
        return LOGGER;
    }

    public static boolean isObfuscated() {
        return obfuscated;
    }

    public static ObfuscationHelper getObfuscationHelper() {
        return ObfuscationHelper.getOrCreateInstanceOf(obfuscated, getLogger());
    }

    public static class Initialization {
        private static List<InitializationListener> listeners = Lists.newArrayList((obfuscated) -> Core.obfuscated = obfuscated);

        public static void registerListener(InitializationListener listener) {
            listeners.add(listener);
        }

        public static List<InitializationListener> getListeners() {
            return Collections.unmodifiableList(listeners);
        }

        public static void finished() {
            listeners = null;
        }
    }

    public interface InitializationListener {
        void onInitialize(boolean obfuscated);
    }
}
