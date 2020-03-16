package dev.fiki.forgehax.main.ui;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class ConsoleEntry {
  private final ConsoleInterface ci;
  private final Date timestamp;
  private final String text;

  @Setter(AccessLevel.NONE)
  private List<String> messages;

  private int ticksExisted = 0;

  ConsoleEntry(ConsoleInterface ci, String text) {
    this.ci = ci;
    this.timestamp = new Date();
    this.text = text;
    updateMessages();
  }

  public int getLineCount() {
    return messages.size();
  }

  public int getAlphaDecay(int defaultAlpha) {
    if(ci.isConsoleOpen() || getTicksExisted() <= ci.getMessageDuration()) {
      return defaultAlpha;
    } else {
      int decayDuration = getTicksExisted() - ci.getMessageDuration();
      int decayRemaining = Math.min(ci.getFadeOutDuration(), Math.max(0, ci.getFadeOutDuration() - decayDuration));
      return (int)((float)defaultAlpha * (decayRemaining / (float)ci.getFadeOutDuration()));
    }
  }

  public void tick() {
    ++ticksExisted;
  }

  public void updateMessages() {
    ImmutableList.Builder<String> lines = ImmutableList.builder();

    float currentLength = 0.f;
    StringBuilder builder = new StringBuilder();

    String last = String.valueOf('\0');

    for(int i = 0; i < text.length(); ++i) {
      String current = String.valueOf(text.charAt(i));
      float size = ci.getTextLength(current);

      boolean isNewLine = current.matches("[\n\r]");
      if(isNewLine || currentLength + size > ci.getLineWidth()) {
        // hit max size, create new builder
        lines.add(builder.toString());
        builder = new StringBuilder();
        currentLength = 0.f;

        // exclude this character
        if(isNewLine) {
          continue;
        }
      }

      // word wrapping
      if(" ".equals(last) && !" ".equals(current)) {
        int nextSpace = text.indexOf(' ', i);
        if(nextSpace > -1) {
          String word = text.substring(i - 1, nextSpace);
          float wordSize = ci.getTextLength(word);
          if(wordSize < ci.getLineWidth()
              && currentLength + wordSize > ci.getLineWidth()) {
            // the word is
            //  1) less than the size of an entire line
            //  2) requires more space than is remaining
            lines.add(builder.toString());
            builder = new StringBuilder();
            currentLength = 0.f;
          }
        }
      }

      // add to buffer
      builder.append(current.replace("\t", "    "));
      currentLength += size;

      last = current;
    }

    if(builder.length() > 0) {
      lines.add(builder.toString());
    }

    this.messages = lines.build();
  }
}
