package dev.fiki.forgehax.main.util.mod;

import dev.fiki.forgehax.main.util.cmd.flag.EnumFlag;

import java.util.Collections;

/**
 * Created on 6/14/2017 by fr1kin
 */
public class ServiceMod extends AbstractMod {
  
  public ServiceMod(String name, String desc) {
    super(Category.SERVICE, name, desc, Collections.singleton(EnumFlag.HIDDEN));
  }
  
  public ServiceMod(String name) {
    this(name, "");
  }
  
  @Override
  public boolean isEnabled() {
    return true;
  }
  
  @Override
  protected void onLoad() { }
  
  @Override
  protected void onUnload() { }
  
  @Override
  protected void onEnabled() { }
  
  @Override
  protected void onDisabled() { }
}
