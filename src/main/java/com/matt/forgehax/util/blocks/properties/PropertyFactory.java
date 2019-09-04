package com.matt.forgehax.util.blocks.properties;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nullable;

/**
 * Created on 6/4/2017 by fr1kin
 */
public class PropertyFactory {

  private static final Map<Class<? extends IBlockProperty>, Supplier<? extends IBlockProperty>>
    PROPERTY_FACTORY = Maps.newHashMap();
  private static final Map<Class<? extends IBlockProperty>, IBlockProperty> CLASS_TO_IMMUTABLE =
    Maps.newHashMap();
  private static final Map<String, Class<? extends IBlockProperty>> HEADING_TO_CLASS =
    Maps.newHashMap();

  public static void registerPropertyFactory(
    Class<? extends IBlockProperty> clazz, Supplier<? extends IBlockProperty> factory) {
    IBlockProperty temp = factory.get();
    CLASS_TO_IMMUTABLE.put(clazz, temp.newImmutableInstance());
    HEADING_TO_CLASS.put(temp.toString(), clazz);
    PROPERTY_FACTORY.put(clazz, factory);
  }

  public static <T extends IBlockProperty> T newInstance(Class<T> clazz) {
    if (clazz != null) {
      Supplier<? extends IBlockProperty> supplier = PROPERTY_FACTORY.get(clazz);
      if (supplier != null) {
        return supplier.get().cast();
      }
    }
    return null;
  }

  public static <T extends IBlockProperty> T newInstance(String name) {
    return newInstance(getClassByName(name));
  }

  public static <T extends IBlockProperty> Class<T> getClassByName(String name) {
    return (Class<T>) HEADING_TO_CLASS.get(name);
  }

  @Nullable
  public static <T extends IBlockProperty> T getImmutableInstance(Class<T> clazz) {
    return clazz != null ? CLASS_TO_IMMUTABLE.get(clazz).cast() : null;
  }

  @Nullable
  public static <T extends IBlockProperty> T getImmutableInstance(String name) {
    return getImmutableInstance(getClassByName(name));
  }

  @Nullable
  public static <T extends IBlockProperty> T newImmutableInstance(Class<T> clazz) {
    IBlockProperty immutable = getImmutableInstance(clazz);
    return immutable != null ? immutable.newImmutableInstance().cast() : null;
  }

  @Nullable
  public static <T extends IBlockProperty> T newImmutableInstance(String name) {
    return newImmutableInstance(getClassByName(name));
  }

  static {
    registerPropertyFactory(BoundProperty.class, BoundProperty::new);
    registerPropertyFactory(ColorProperty.class, ColorProperty::new);
    registerPropertyFactory(DimensionProperty.class, DimensionProperty::new);
    registerPropertyFactory(TagProperty.class, TagProperty::new);
    registerPropertyFactory(ToggleProperty.class, ToggleProperty::new);
  }
}
