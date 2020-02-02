package dev.fiki.forgehax.asm;

import org.apache.logging.log4j.Logger;

/**
 * Created on 5/2/2017 by fr1kin
 */
public interface ASMCommon {
  static Logger getLogger() {
    return ForgeHaxCoreTransformer.getLogger();
  }
}
