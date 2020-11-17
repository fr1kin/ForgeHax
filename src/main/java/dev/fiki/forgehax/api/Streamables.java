package dev.fiki.forgehax.api;

import com.google.common.collect.Iterators;

import java.util.Enumeration;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created on 2/15/2018 by fr1kin
 */
public class Streamables {
  
  public static <T> Stream<T> enumerationStream(Enumeration<T> enumeration) {
    return StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(
            Iterators.forEnumeration(enumeration), Spliterator.ORDERED),
        false);
  }
}
