package dev.fiki.javac.remapper;

import dev.fiki.javac.plugin.type.TypeUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class TypeUtilTest {
  @Test
  void getInternalClassName() {

  }

  @Test
  void getDescriptorParameters() {
    String[] got = TypeUtil.getDescriptorParameters("(Ljava/lang/Object;IIIZ)Ljava/lang/Object;");
    String[] expected = new String[] {
        "java/lang/Object",
        "I",
        "I",
        "I",
        "Z",
    };

    assertArrayEquals(expected, got);
  }

  @Test
  void getDescriptorParameters_ArrayParams() {
    String[] got = TypeUtil.getDescriptorParameters("([Ljava/lang/Object;[I[[I[[[[[[[IZ)Ljava/lang/Object;");
    String[] expected = new String[] {
        "java/lang/Object",
        "I",
        "I",
        "I",
        "Z",
    };

    System.err.println(String.join(",", got));
    assertArrayEquals(expected, got);
  }
}
