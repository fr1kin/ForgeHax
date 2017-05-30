package com.matt.forgehax.asm.utils;

/**
 * Created on 5/29/2017 by fr1kin
 */
public class CurrentBuildMapping {
    private static String MC = "1.11";
    private static String CHANNEL = "snapshot";
    private static String VERSION = "20161220";

    //TODO: find a way to get the current mapping version from the build.gradle
    public static String getMapping() {
        return String.format("%s_%s_%s", MC, CHANNEL, VERSION);
    }
}
