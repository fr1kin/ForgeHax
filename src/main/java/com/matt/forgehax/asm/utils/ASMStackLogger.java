package com.matt.forgehax.asm.utils;

import com.google.common.base.Throwables;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Created on 5/4/2017 by fr1kin */
public class ASMStackLogger {
  private static final Logger STACK_LOGGER = LogManager.getLogger("ForgeHaxAsmStackTrace");

  public static void printStackTrace(Throwable e) {
    STACK_LOGGER.error(Throwables.getStackTraceAsString(e));
  }
}
