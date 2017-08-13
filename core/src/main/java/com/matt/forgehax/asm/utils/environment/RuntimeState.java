package com.matt.forgehax.asm.utils.environment;

import com.matt.forgehax.asm.utils.remapping.NonObfuscatedStateMapper;
import com.matt.forgehax.asm.utils.remapping.ObfuscatedStateMapper;

/**
 * Created on 5/26/2017 by fr1kin
 */
public class RuntimeState {
    private static State state = State.OBFUSCATED; // always assume the state is obfuscated because core minecraft classes will load before forge feeds us the obfuscation state
    private static IStateMapper remapper = null;

    public static void setState(State state) {
        RuntimeState.state = state;
    }

    public static void markAsNormal() {
        setState(State.NORMAL);
    }

    public static void markAsSrg() {
        setState(State.SRG);
    }

    public static void markAsObfuscated() {
        setState(State.OBFUSCATED);
    }

    public static boolean isNormal() {
        return state.equals(State.NORMAL);
    }

    public static boolean isSrg() {
        return state.equals(State.SRG);
    }

    public static boolean isObfuscated() {
        return state.equals(State.OBFUSCATED);
    }

    public static State getState() {
        return state;
    }

    public static IStateMapper getMapper() {
        return remapper == null ? remapper = (isObfuscated() ? ObfuscatedStateMapper.getInstance() : NonObfuscatedStateMapper.getInstance()) : remapper;
    }
}
