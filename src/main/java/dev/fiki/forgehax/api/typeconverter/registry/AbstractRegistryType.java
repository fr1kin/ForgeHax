package dev.fiki.forgehax.api.typeconverter.registry;

import dev.fiki.forgehax.api.typeconverter.TypeConverter;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.Comparator;

abstract class AbstractRegistryType<E extends IForgeRegistryEntry<E>> extends TypeConverter<E> {
  public abstract IForgeRegistry<E> getRegistry();

  @Override
  public String label() {
    return getRegistry().getRegistryName().toString();
  }

  @Override
  public Class<E> type() {
    return getRegistry().getRegistrySuperType();
  }

  @Override
  public E parse(String value) {
    return getRegistry().getValue(new ResourceLocation(value));
  }

  @Override
  public String convert(E value) {
    ResourceLocation resource = value.getRegistryName();
    return resource == null
        ? getRegistry().getKey(value).toString()
        : resource.toString();
  }

  @Override
  public Comparator<E> comparator() {
    return ((o1, o2) -> {
      if(o1 instanceof Comparator) {
        return ((Comparator) o1).compare(o1, o2);
      } else if(o1 instanceof Comparable) {
        return ((Comparable) o1).compareTo(o2);
      }
      return AbstractRegistryType.super.comparator().compare(o1, o2);
    });
  }
}
