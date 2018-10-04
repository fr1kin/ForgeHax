package com.matt.forgehax.util.console;

/** Created on 6/10/2017 by fr1kin */
public interface ConsoleWriter {
  default void write(String msg) {
    ConsoleIO.write(msg);
  }

  default void incrementIndent() {
    ConsoleIO.incrementIndent();
  }

  default void decrementIndent() {
    ConsoleIO.decrementIndent();
  }
}
