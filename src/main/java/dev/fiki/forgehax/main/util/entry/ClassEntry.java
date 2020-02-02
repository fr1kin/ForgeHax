package dev.fiki.forgehax.main.util.entry;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.fiki.forgehax.main.util.serialization.ISerializableJson;
import java.io.IOException;
import java.util.Objects;
import javax.annotation.Nullable;

import static dev.fiki.forgehax.main.Common.getLauncherClassLoader;

/**
 * Created on 10/13/2017 by fr1kin
 */
public class ClassEntry implements ISerializableJson {
  
  private final String clazzName;
  private Class<?> clazz;
  
  public ClassEntry(String clazzName) {
    this.clazzName = clazzName;
    getClassInstance(); // initial attempt
  }
  
  public ClassEntry(Class<?> clazz) {
    Objects.requireNonNull(clazz);
    this.clazzName = clazz.getCanonicalName();
    this.clazz = clazz;
  }
  
  public String getClassName() {
    return clazzName;
  }
  
  @Nullable
  public Class<?> getClassInstance() {
    if (clazz == null) {
      try {
        clazz = Class.forName(clazzName, true, getLauncherClassLoader());
      } catch (Throwable t) {
      }
    }
    return clazz;
  }
  
  @Override
  public void serialize(JsonWriter writer) throws IOException {
    writer.beginObject();
    writer.endObject();
  }
  
  @Override
  public void deserialize(JsonReader reader) throws IOException {
    reader.beginObject();
    reader.endObject();
  }
  
  @Override
  public boolean equals(Object obj) {
    return obj == this
        || (obj instanceof ClassEntry && clazzName.equals(((ClassEntry) obj).clazzName))
        || (obj instanceof String && clazzName.equals(obj))
        || (getClassInstance() != null
        && obj != null
        && obj instanceof Class
        && getClassInstance().equals(obj));
  }
  
  @Override
  public int hashCode() {
    return clazzName.toLowerCase().hashCode();
  }
  
  @Override
  public String toString() {
    return clazzName;
  }
}
