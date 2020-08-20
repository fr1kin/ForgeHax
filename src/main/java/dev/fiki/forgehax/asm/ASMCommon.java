package dev.fiki.forgehax.asm;

import dev.fiki.forgehax.common.LoggerProvider;
import org.apache.logging.log4j.Logger;

/**
 * Created on 5/2/2017 by fr1kin
 */
public interface ASMCommon {
  Logger LOGGER = LoggerProvider.builder()
      .contextClass(ForgeHaxCoreTransformer.class)
      .label("core")
      .build()
      .getLogger();;

  static Logger getLogger() {
    return LOGGER;
  }

  default Logger getLog() {
    return LOGGER;
  }
}
