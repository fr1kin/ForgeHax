package dev.fiki.forgehax.main.util.mod;

import dev.fiki.forgehax.main.util.cmd.flag.EnumFlag;

/**
 * Created on 6/14/2017 by fr1kin
 */
public class ServiceMod extends AbstractMod {

  public ServiceMod() {
    super();
    addFlag(EnumFlag.HIDDEN);
    addFlag(EnumFlag.SERVICE_MOD);
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
