package com.matt.forgehax.util.spam;

import org.junit.Assert;
import org.junit.Test;

/** Created on 7/20/2017 by fr1kin */
public class SpamTokensTest {
  @Test
  public void testEscapedStrings() {
    String input = "This is a test for string replacement {PLAYER_NAME}";
    String arg = "\\this is an attempted escape \\n \\";

    String result = SpamTokens.PLAYER_NAME.fill(input, arg);

    Assert.assertEquals(
        result, "This is a test for string replacement \\this is an attempted escape \\n \\");
  }
}
