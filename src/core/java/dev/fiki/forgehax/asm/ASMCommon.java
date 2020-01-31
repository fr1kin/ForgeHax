package dev.fiki.forgehax.asm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created on 5/2/2017 by fr1kin
 */
public interface ASMCommon {
  Logger LOGGER = LogManager.getLogger("ForgeHaxCore");

  static Logger getLogger() {
    return LOGGER;
  }
}
