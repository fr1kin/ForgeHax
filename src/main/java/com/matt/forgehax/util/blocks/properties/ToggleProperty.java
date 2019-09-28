package com.matt.forgehax.util.blocks.properties;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;

/**
 * Created on 5/24/2017 by fr1kin
 */
public class ToggleProperty implements IBlockProperty {
  
  private static final String HEADING = "enabled";
  
  private boolean enabled = true;
  
  public boolean isEnabled() {
    return enabled;
  }
  
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
  
  public void enable() {
    setEnabled(true);
  }
  
  public void disable() {
    setEnabled(false);
  }
  
  public void toggle() {
    if (enabled) {
      disable();
    } else {
      enable();
    }
  }
  
  @Override
  public void serialize(JsonWriter writer) throws IOException {
    writer.value(enabled);
  }
  
  @Override
  public void deserialize(JsonReader reader) throws IOException {
    setEnabled(reader.nextBoolean());
  }
  
  @Override
  public boolean isNecessary() {
    return !enabled;
  }
  
  @Override
  public String helpText() {
    return Boolean.toString(enabled);
  }
  
  @Override
  public IBlockProperty newImmutableInstance() {
    return new ImmutableToggle();
  }
  
  @Override
  public String toString() {
    return HEADING;
  }
  
  private static class ImmutableToggle extends ToggleProperty {
    
    @Override
    public boolean isEnabled() {
      return true;
    }
    
    @Override
    public void setEnabled(boolean enabled) {
    }
    
    @Override
    public void enable() {
    }
    
    @Override
    public void disable() {
    }
    
    @Override
    public void toggle() {
    }
  }
}
