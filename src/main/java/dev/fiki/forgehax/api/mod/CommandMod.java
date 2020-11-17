package dev.fiki.forgehax.api.mod;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import dev.fiki.forgehax.api.cmd.ICommand;
import dev.fiki.forgehax.api.cmd.flag.EnumFlag;

import static dev.fiki.forgehax.main.Common.getRootCommand;

/**
 * Created on 6/1/2017 by fr1kin
 */
public class CommandMod extends AbstractMod {

  public CommandMod() {
    super(null);
    deleteFlag(EnumFlag.SERIALIZED_NODE);
  }

  @Override
  public boolean isHidden() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return false;
  }

  @Override
  protected void onLoad() { }

  @Override
  protected void onUnload() { }

  @Override
  protected final void onEnabled() { }

  @Override
  protected final void onDisabled() { }

  @Override
  public boolean addChild(ICommand command) {
    return getRootCommand().addChild(command);
  }

  @Override
  public JsonElement serialize() {
    return JsonNull.INSTANCE;
  }

  @Override
  public void deserialize(JsonElement json) {
  }

  @Override
  public boolean writeConfiguration() {
    return false;
  }

  @Override
  public boolean readConfiguration() {
    return false;
  }
}
