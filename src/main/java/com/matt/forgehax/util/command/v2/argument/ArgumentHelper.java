package com.matt.forgehax.util.command.v2.argument;

import java.util.Collection;

/**
 * Created on 1/30/2018 by fr1kin
 */
public class ArgumentHelper {
    public static boolean isNullOrEmpty(ArgumentV2<?> argument) {
        return argument == null || argument instanceof ArgumentV2Empty;
    }

    /**
     * Checks the required/non-required argument order to ensure that required argument(s) are not
     * put after a non-required argument.
     * @param arguments Array of arguments
     * @return true if the order is wrong
     */
    public static boolean isInvalidArgumentOrder(Collection<ArgumentV2<?>> arguments) {
        boolean allowed = true;
        for(ArgumentV2<?> arg : arguments) {
            if(!arg.isRequired())
                allowed = false; // required arguments are not allowed after non-required arguments now
            else if(!allowed) // this is a required argument, if allowed=false then you have tried to added a required argument after a non-required argument
                return true;
        }
        return false;
    }
}
