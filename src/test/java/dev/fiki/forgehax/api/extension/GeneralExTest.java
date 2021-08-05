package dev.fiki.forgehax.api.extension;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class GeneralExTest {

  @Test
  void globToRegex() {
    // sanity checks

    assertThat(GeneralEx.globToRegex("*foobar")).isEqualTo("^.*\\Qfoobar\\E$");
    assertThat(GeneralEx.globToRegex("foobar*")).isEqualTo("^\\Qfoobar\\E.*$");
    assertThat(GeneralEx.globToRegex("*")).isEqualTo("^.*$");
    assertThat(GeneralEx.globToRegex("?")).isEqualTo("^.$");

    // *
    {
      assertThat(GeneralEx.globToRegex("123*9"))
          .isEqualTo("^\\Q123\\E.*\\Q9\\E$")
          .satisfies(allOf(
              new Condition<>("1239"::matches, "0 chars"),
              new Condition<>("123x9"::matches, "single char"),
              new Condition<>("123xxxxx9"::matches, "multiple chars"),
              not(new Condition<>(""::matches, "empty string")),
              not(new Condition<>("123"::matches, "missing 9")),
              not(new Condition<>("9"::matches, "missing 123")),
              not(new Condition<>("01239"::matches, "0 prefix")),
              not(new Condition<>("12390"::matches, "0 suffix"))
          ));

      assertThat(GeneralEx.globToRegex("foo_***_bar"))
          .describedAs("multiple asterisks should be condensed down to one")
          .isEqualTo("^\\Qfoo_\\E.*\\Q_bar\\E$");

      assertThat(GeneralEx.globToRegex("*AAA*BBB*CCC*"))
          .satisfies(allOf(
              new Condition<>("AAABBBCCC"::matches, "fill 0 per wildcard"),
              new Condition<>("xAAAxBBBxCCCx"::matches, "fill 1 per wildcard")
          ));
    }

    // ?
    {
      assertThat(GeneralEx.globToRegex("123?9"))
          .isEqualTo("^\\Q123\\E.\\Q9\\E$")
          .satisfies(allOf(
              new Condition<>("123x9"::matches, "1 char"),
              not(new Condition<>(""::matches, "empty string")),
              not(new Condition<>("123x"::matches, "missing 9")),
              not(new Condition<>("x9"::matches, "missing 123")),
              not(new Condition<>("0123x9"::matches, "0 prefix")),
              not(new Condition<>("123x90"::matches, "0 suffix"))
          ));

      assertThat(GeneralEx.globToRegex("123???9"))
          .isEqualTo("^\\Q123\\E...\\Q9\\E$")
          .satisfies(allOf(
              new Condition<>("123xxx9"::matches, "all chars"),
              not(new Condition<>(""::matches, "empty string")),
              not(new Condition<>("1239"::matches, "missing all chars")),
              not(new Condition<>("123x9"::matches, "missing 2 chars")),
              not(new Condition<>("123xx9"::matches, "missing 1 char"))
          ));
    }

    // strange combos

    assertThat(GeneralEx.globToRegex("123*??*9"))
        .describedAs("both wildcard types")
        .isEqualTo("^\\Q123\\E.*...*\\Q9\\E$")
        .satisfies(allOf(
            new Condition<>("123xxxyyxxx9"::matches, "all chars"),
            new Condition<>("123yy9"::matches, "only single wildcards"),
            not(new Condition<>(""::matches, "empty string")),
            not(new Condition<>("1239"::matches, "missing all chars")),
            not(new Condition<>("123y9"::matches, "missing 1 char"))
        ));
  }
}
