package com.matt.forgehax.util.command.v2.exception;

import com.google.common.base.Strings;
import com.matt.forgehax.util.command.v2.CommandHelperV2;

/**
 * Created on 2/3/2018 by fr1kin
 */
public class CommandExceptions {
    public static void checkIfNull(Object o, String msg) throws CommandRuntimeExceptionV2.NullPointer {
        if(o == null) throw new CommandRuntimeExceptionV2.NullPointer(msg);
    }
    public static void checkIfNull(Object o) throws CommandRuntimeExceptionV2.NullPointer {
        checkIfNull(o, String.valueOf(o) + " is null");
    }

    public static void checkIfNullOrEmpty(String o, String message) throws CommandRuntimeExceptionV2 {
        if(Strings.isNullOrEmpty(o)) throw new CommandRuntimeExceptionV2(message);
    }

    public static void checkIfNameValid(String name, String message) throws CommandRuntimeExceptionV2 {
        if(!CommandHelperV2.isNameValid(name)) throw new CommandRuntimeExceptionV2(message);
    }
}
