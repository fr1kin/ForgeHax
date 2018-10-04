package com.matt.forgehax.util.mod;

import com.matt.forgehax.util.command.callbacks.CallbackData;

/** Created on 6/14/2017 by fr1kin */
public class ServiceMod extends BaseMod {
  public ServiceMod(String name, String desc) {
    super(Category.SERVICE, name, desc);
  }

  public ServiceMod(String name) {
    super(Category.SERVICE, name);
  }

  @Override
  public boolean isHidden() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  protected void onLoad() {}

  @Override
  protected void onUnload() {}

  @Override
  protected void onEnabled() {}

  @Override
  protected void onDisabled() {}

  @Override
  protected void onBindPressed(CallbackData cb) {}

  @Override
  protected void onBindKeyDown(CallbackData cb) {}
}
