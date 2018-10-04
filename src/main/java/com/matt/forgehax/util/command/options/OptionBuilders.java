package com.matt.forgehax.util.command.options;

import java.util.Arrays;
import joptsimple.OptionParser;

/** Created on 6/6/2017 by fr1kin */
public class OptionBuilders {
  public static void rgba(OptionParser parser) {
    parser.acceptsAll(Arrays.asList("red", "r"), "red").withRequiredArg();
    parser.acceptsAll(Arrays.asList("green", "g"), "green").withRequiredArg();
    parser.acceptsAll(Arrays.asList("blue", "b"), "blue").withRequiredArg();
    parser.acceptsAll(Arrays.asList("alpha", "a"), "alpha").withRequiredArg();
  }

  public static void meta(OptionParser parser) {
    parser.acceptsAll(Arrays.asList("meta", "m"), "blocks metadata id").withRequiredArg();
  }

  public static void id(OptionParser parser) {
    parser.acceptsAll(Arrays.asList("id", "i"), "searches for block by id instead of name");
  }

  public static void regex(OptionParser parser) {
    parser.acceptsAll(
        Arrays.asList("regex", "e"),
        "searches for blocks by using the argument as a regex expression");
  }

  public static void bounds(OptionParser parser) {
    parser
        .accepts("bounds", "Will only draw blocks from within the min-max bounds given")
        .withRequiredArg();
  }
}
