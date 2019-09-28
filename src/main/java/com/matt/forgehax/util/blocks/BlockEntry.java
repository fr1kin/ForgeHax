package com.matt.forgehax.util.blocks;

import com.google.common.collect.Maps;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.matt.forgehax.Globals;
import com.matt.forgehax.util.blocks.exceptions.BlockDoesNotExistException;
import com.matt.forgehax.util.blocks.properties.IBlockProperty;
import com.matt.forgehax.util.blocks.properties.PropertyFactory;
import com.matt.forgehax.util.serialization.ISerializableJson;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import joptsimple.internal.Strings;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

/**
 * Created on 5/19/2017 by fr1kin
 */
public class BlockEntry implements ISerializableJson, Globals {
  
  private enum OpenMode {
    READ,
    WRITE,
    ;
  }
  
  private final Map<Class<? extends IBlockProperty>, IBlockProperty> properties = Maps.newHashMap();
  
  private final String uniqueId;
  
  private final Block block;
  private final int meta;
  
  public BlockEntry(String uniqueId) {
    this.uniqueId = uniqueId;
    Block block;
    int meta;
    try {
      BlockOptionHelper.BlockData data = BlockOptionHelper.fromUniqueName(uniqueId);
      block = data.block;
      meta = data.meta;
    } catch (Throwable t) {
      block = null;
      meta = -1;
    }
    this.block = block;
    this.meta = meta;
  }
  
  public BlockEntry(Block block, int meta, boolean check) throws BlockDoesNotExistException {
    meta = Math.max(meta, 0);
    if (check) {
      BlockOptionHelper.requiresValidBlock(block, meta);
    }
    this.block = block;
    this.meta = BlockOptionHelper.getAllBlocks(block).size() > 1 ? meta : -1;
    this.uniqueId = getResourceName() + (isMetadata() ? ("::" + getMetadata()) : Strings.EMPTY);
  }
  
  public BlockEntry(String name, int meta) throws BlockDoesNotExistException {
    this(Block.getBlockFromName(name), meta, !BlockOptionHelper.isAir(name));
  }
  
  public BlockEntry(int id, int meta) throws BlockDoesNotExistException {
    this(Block.getBlockById(id), meta, !BlockOptionHelper.isAir(id));
  }
  
  protected void registerProperty(IBlockProperty property) {
    properties.put(property.getClass(), property);
  }
  
  protected void unregisterProperty(IBlockProperty property) {
    properties.remove(property.getClass());
  }
  
  public Collection<IBlockProperty> getProperties() {
    return Collections.unmodifiableCollection(properties.values());
  }
  
  public String getUniqueName() {
    return uniqueId;
  }
  
  public String getResourceName() {
    return block != null
        ? (block.getRegistryName() != null ? block.getRegistryName().toString() : block.toString())
        : Strings.EMPTY;
  }
  
  public String getPrettyName() {
    return block != null
        ? ((block.getRegistryName() != null
        ? block.getRegistryName().getResourcePath()
        : block.toString())
        + (isMetadata() ? ":" + meta : Strings.EMPTY))
        : Strings.EMPTY;
  }
  
  public Block getBlock() {
    return block;
  }
  
  public int getMetadata() {
    return meta;
  }
  
  public boolean isMetadata() {
    return meta > -1;
  }
  
  private <T extends IBlockProperty> T getProperty(@Nonnull Class<T> clazz, OpenMode mode) {
    switch (mode) {
      case READ:
        return properties.getOrDefault(clazz, PropertyFactory.getImmutableInstance(clazz)).cast();
      case WRITE:
        return properties
            .computeIfAbsent(
                clazz,
                c -> {
                  IBlockProperty property = PropertyFactory.newInstance(c);
                  registerProperty(property);
                  return property;
                })
            .cast();
    }
    throw new IllegalArgumentException(
        String.format("No such property \"%s\" (Possibly not registered?)", clazz.getSimpleName()));
  }
  
  /**
   * Allows null pointer free reading of the property in question. If this class does not have a
   * unique property for the entry in question, an immutable entry will be returned. Mutating data
   * is possible, but is not safe. If you need to mutate data use getWritableProperty
   *
   * @param clazz Property class to access
   * @param <T> Property type
   * @return instance of the class in question
   */
  @Nonnull
  public <T extends IBlockProperty> T getReadableProperty(@Nonnull Class<T> clazz) {
    return getProperty(clazz, OpenMode.READ);
  }
  
  /**
   * Allows null pointer free reading AND writing of the property in question. If this class does
   * not have a unique property for the entry in question, an immutable entry will be returned.
   *
   * @param clazz Property class to access
   * @param <T> Property type
   * @return instance of the class in question
   */
  @Nonnull
  public <T extends IBlockProperty> T getWritableProperty(@Nonnull Class<T> clazz) {
    return getProperty(clazz, OpenMode.WRITE);
  }
  
  /**
   * Unregisters any property that is not necessary. Call this after using getWritableProperty
   */
  public void cleanupProperties() {
    for (IBlockProperty property : properties.values()) {
      if (!property.isNecessary()) {
        unregisterProperty(property);
      }
    }
  }
  
  boolean isEqual(Block block, int meta) {
    return Objects.equals(getBlock(), block) && (!isMetadata() || (getMetadata() == meta));
  }
  
  public String helpText() {
    StringBuilder builder = new StringBuilder(getPrettyName());
    builder.append(" {");
    Iterator<? extends IBlockProperty> it = getProperties().iterator();
    while (it.hasNext()) {
      IBlockProperty option = it.next();
      builder.append(option.toString());
      builder.append("=");
      builder.append(option.helpText());
      if (it.hasNext()) {
        builder.append(", ");
      }
    }
    builder.append("}");
    return builder.toString();
  }
  
  @Override
  public void serialize(final JsonWriter writer) throws IOException {
    cleanupProperties();
    
    writer.beginObject();
    for (IBlockProperty property : properties.values()) {
      writer.name(property.toString());
      property.serialize(writer);
    }
    writer.endObject();
  }
  
  @Override
  public void deserialize(final JsonReader reader) throws IOException {
    reader.beginObject();
    while (reader.hasNext()) {
      String name = reader.nextName();
      IBlockProperty property = PropertyFactory.newInstance(name);
      if (property != null) {
        property.deserialize(reader);
        registerProperty(property);
      } else {
        LOGGER.warn(String.format("\"%s\" is not a valid property name", name));
      }
    }
    reader.endObject();
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof BlockEntry) {
      return getUniqueName().compareTo(((BlockEntry) obj).getUniqueName()) == 0;
    } else if (obj instanceof IBlockState) {
      return isEqual(
          ((IBlockState) obj).getBlock(),
          ((IBlockState) obj).getBlock().getMetaFromState((IBlockState) obj));
    } else {
      return hashCode() == obj.hashCode();
    }
  }
  
  @Override
  public int hashCode() {
    return getUniqueName().hashCode();
  }
  
  @Override
  public String toString() {
    return getUniqueName();
  }
}
