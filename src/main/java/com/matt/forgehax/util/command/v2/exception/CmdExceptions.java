package com.matt.forgehax.util.command.v2.exception;

import com.google.common.base.Strings;
import com.matt.forgehax.util.command.v2.CmdHelper;

/**
 * Created on 2/3/2018 by fr1kin
 */
public class CmdExceptions {
    public static void checkIfNull(Object o, String msg) throws CmdRuntimeException.NullPointer {
        if(o == null) throw new CmdRuntimeException.NullPointer(msg);
    }
    public static void checkIfNull(Object o) throws CmdRuntimeException.NullPointer {
        checkIfNull(o, String.valueOf(o) + " is null");
    }

    public static void checkIfNullOrEmpty(String o, String message) throws CmdRuntimeException {
        if(Strings.isNullOrEmpty(o)) throw new CmdRuntimeException(message);
    }

    public static void checkIfNameValid(String name, String message) throws CmdRuntimeException {
        if(!CmdHelper.isNameValid(name)) throw new CmdRuntimeException(message);
    }
}
