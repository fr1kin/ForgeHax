package com.matt.forgehax.util.console;

import com.matt.forgehax.Globals;
import com.matt.forgehax.Wrapper;

/**
 * Created on 6/10/2017 by fr1kin
 */
public class ConsoleIO implements Globals {
    public static void write(String msg) {
        Wrapper.printMessageNaked(">> ", msg); //TODO: use a non-chat console
    }
}
