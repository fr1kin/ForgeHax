package com.matt.forgehax.util.entry;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.matt.forgehax.util.serialization.ISerializableJson;
import java.io.IOException;
import java.util.Objects;
import net.minecraft.util.EnumFacing;

public class FacingEntry implements ISerializableJson {
  
  private final EnumFacing facing;
  
  public FacingEntry(EnumFacing facing) {
    Objects.requireNonNull(facing);
    this.facing = facing;
  }
  
  public FacingEntry(String str) {
    this(EnumFacing.byName(str));
  }
  
  public EnumFacing getFacing() {
    return facing;
  }
  
  @Override
  public void serialize(JsonWriter writer) throws IOException {
    writer.beginArray();
    writer.endArray();
  }
  
  @Override
  public void deserialize(JsonReader reader) throws IOException {
    reader.beginArray();
    reader.endArray();
  }
  
  @Override
  public boolean equals(Object obj) {
    return this == obj
      || (obj instanceof FacingEntry && facing.equals(((FacingEntry) obj).getFacing()))
      || (obj instanceof EnumFacing && facing.equals(obj));
  }
  
  @Override
  public String toString() {
    return getFacing().getName2();
  }
}
