package com.matt.forgehax.asm.utils.environment;

import com.matt.forgehax.asm.utils.remapping.NonObfuscatedStateMapper;
import com.matt.forgehax.asm.utils.remapping.SrgStateMapper;

/** Created on 5/26/2017 by fr1kin */
public class RuntimeState {
  /** Used for remapping Notch to SRG and obfuscated naming conventions */
  private static IStateMapper remapper = null;

  private static State state;

  public static State getState() {
    return state;
  }


  public static void initializeWithState(State stateIn) {
    if (state != null) {
      throw new IllegalStateException("State has already been set to " + state);
    }
    state = stateIn;
    remapper = stateIn.equals(State.SRG) ? SrgStateMapper.getInstance() : NonObfuscatedStateMapper.getInstance();
  }

  private static void assertIsInitialized() {
    if (state == null) {
      throw new IllegalStateException("RuntimeState has not been initialized");
    }
  }

  public static boolean isNormal() {
    assertIsInitialized();
    return getState().equals(State.NORMAL);
  }

  public static boolean isSrg() {
    assertIsInitialized();
    return getState().equals(State.SRG);
  }

  public static IStateMapper getMapper() {
    assertIsInitialized();
    return remapper;
  }
}
