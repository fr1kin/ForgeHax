package dev.fiki.forgehax.api;

import net.minecraft.util.text.*;

public class TextComponentBuilder {
  public static TextComponentBuilder builder() {
    return new TextComponentBuilder();
  }

  private final ITextComponent root;
  private ITextComponent current;

  private TextComponentBuilder() {
    this.root = new StringTextComponent("");
    this.current = root;
  }

  private Style style() {
    return current.getStyle();
  }

  public TextComponentBuilder color(TextFormatting color) {
    style().withColor(Color.fromLegacyFormat(color));
    return this;
  }

  public TextComponentBuilder bold(boolean bold) {
    style().withBold(bold);
    return this;
  }

  public TextComponentBuilder italic(boolean italic) {
    style().withItalic(italic);
    return this;
  }

  public TextComponentBuilder strikeThrough(boolean strikeThrough) {
    style().setStrikethrough(strikeThrough);
    return this;
  }

  public TextComponentBuilder underlined(boolean underlined) {
    style().setUnderlined(underlined);
    return this;
  }

  public TextComponentBuilder obfuscated(boolean obfuscated) {
    style().setObfuscated(obfuscated);
    return this;
  }

  public TextComponentBuilder text(String text) {
    StringTextComponent txt = new StringTextComponent(text);
    current.getSiblings().add(txt);
    current = txt;
    return this;
  }

  public ITextComponent build() {
    return root;
  }
}
