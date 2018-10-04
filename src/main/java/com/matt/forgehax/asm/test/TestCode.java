package com.matt.forgehax.asm.test;

import com.google.common.collect.Lists;
import java.util.List;

/** Created on 9/4/2016 by fr1kin */
public class TestCode {
  private static boolean isNoSlowOn = false;
  private static int empty = 0;

  private boolean movementInput = false;
  private boolean onGround = false;

  private double stepHeight = 0;

  public boolean isSneaking() {
    return true;
  }

  public boolean isHandActive() {
    return true;
  }

  public boolean isRiding() {
    return true;
  }

  public void moveEntity(Float a1, Float a2, Float a3, Object o) {
    boolean flag;
    if (flag = !isSneaking()) {
      double local1, l2, l3, l4;
      isHandActive();
      for (int i = 0; i < 4; i++) ;
      local1 = 12310;
      l2 = 457;
      l3 = 6435;
      l4 = 1243;
    }
    if (flag) {
      isRiding();
    }

    List<Object> list = Lists.newArrayList();
    return;
  }
}
