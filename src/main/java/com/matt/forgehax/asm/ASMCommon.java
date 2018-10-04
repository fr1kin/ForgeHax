package com.matt.forgehax.asm;

import com.matt.forgehax.asm.utils.environment.IStateMapper;
import com.matt.forgehax.asm.utils.environment.RuntimeState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Created on 5/2/2017 by fr1kin */
public interface ASMCommon {
  Logger LOGGER = LogManager.getLogger("ForgeHaxASM");
  IStateMapper MAPPER = RuntimeState.getMapper();
}
