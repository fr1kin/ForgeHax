package com.matt.forgehax.util.command;

import com.matt.forgehax.util.serialization.ISerializableJson;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created on 6/5/2017 by fr1kin
 */
public class OptionsBuilder<E extends ISerializableJson>
    extends BaseCommandBuilder<OptionsBuilder<E>, Options<E>> {
  
  public OptionsBuilder<E> supplier(Supplier<Collection<E>> supplier) {
    return insert(Options.SUPPLIER, supplier);
  }
  
  public OptionsBuilder<E> factory(Function<String, E> factory) {
    return insert(Options.FACTORY, factory);
  }
  
  public OptionsBuilder<E> defaults(Supplier<Collection<E>> defaults) {
    return insert(Options.DEFAULTS, defaults);
  }
  
  @Override
  public Options<E> build() {
    return new Options<E>(data);
  }
}
