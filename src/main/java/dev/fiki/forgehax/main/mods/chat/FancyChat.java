package dev.fiki.forgehax.main.mods.chat;

import dev.fiki.forgehax.api.cmd.settings.EnumSetting;
import dev.fiki.forgehax.api.cmd.settings.PatternSetting;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.extension.GeneralEx;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.asm.events.packet.PacketOutboundEvent;
import dev.fiki.forgehax.main.Common;
import lombok.experimental.ExtensionMethod;
import net.minecraft.network.play.client.CChatMessagePacket;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// made by BABBAJ

//@RegisterMod(
//    name = "FancyChat",
//    description = "meme text",
//    category = Category.CHAT
//)
@ExtensionMethod({GeneralEx.class})
public class FancyChat extends ToggleMod {

  private enum Mode {
    FULL_WIDTH,
    CIRCLE,
    PARENTHESES,
    SMALL,
    LEET,
    WAVE,
    RANDOMCASE
  }

  private static final String alphabet =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
  private static final int[][] FONT = {
      {
          0xFF21, 0xFF22, 0xFF23, 0xFF24, 0xFF25, 0xFF26, 0xFF27, 0xFF28, 0xFF29, 0xFF2A, 0xFF2B,
          0xFF2C, 0xFF2D, 0xFF2E, 0xFF2F, 0xFF30, 0xFF31, 0xFF32, 0xFF33, 0xFF34, 0xFF35, 0xFF36,
          0xFF37, 0xFF38, 0xFF39, 0xFF3A, 0xFF41, 0xFF42, 0xFF43, 0xFF44, 0xFF45, 0xFF46, 0xFF47,
          0xFF48, 0xFF49, 0xFF4A, 0xFF4B, 0xFF4C, 0xFF4D, 0xFF4E, 0xFF4F, 0xFF50, 0xFF51, 0xFF52,
          0xFF53, 0xFF54, 0xFF55, 0xFF56, 0xFF57, 0xFF58, 0xFF59, 0xFF5A, 0xFF10, 0xFF11, 0xFF12,
          0xFF13, 0xFF14, 0xFF15, 0xFF16, 0xFF17, 0xFF18, 0xFF19
      },
      { // Enclosed alphanumerics
          0x24B6, 0x24B7, 0x24B8, 0x24B9, 0x24BA, 0x24BB, 0x24BC, 0x24BD, 0x24BE, 0x24BF, 0x24C0,
          0x24C1, 0x24C2, 0x24C3, 0x24C4, 0x24C5, 0x24C6, 0x24C7, 0x24C8, 0x24C9, 0x24CA, 0x24CB,
          0x24CC, 0x24CD, 0x24CE, 0x24CF, 0x24D0, 0x24D1, 0x24D2, 0x24D3, 0x24D4, 0x24D5, 0x24D6,
          0x24D7, 0x24D8, 0x24D9, 0x24DA, 0x24DB, 0x24DC, 0x24DD, 0x24DE, 0x24DF, 0x24E0, 0x24E1,
          0x24E2, 0x24E3, 0x24E4, 0x24E5, 0x24E6, 0x24E7, 0x24E8, 0x24E9, 0x24EA, 0x2460, 0x2461,
          0x2462, 0x2463, 0x2464, 0x2465, 0x2466, 0x2467, 0x2468
      },
      { // Enclosed alphanumerics, no "zero"
          0x249C, 0x249D, 0x249E, 0x249F, 0x24A0, 0x24A1, 0x24A2, 0x24A3, 0x24A4, 0x24A5, 0x24A6,
          0x24A7, 0x24A8, 0x24A9, 0x24AA, 0x24AB, 0x24AC, 0x24AD, 0x24AE, 0x24AF, 0x24B0, 0x24B1,
          0x24B2, 0x24B3, 0x24B4, 0x24B5, 0x249C, 0x249D, 0x249E, 0x249F, 0x24A0, 0x24A1, 0x24A2,
          0x24A3, 0x24A4, 0x24A5, 0x24A6, 0x24A7, 0x24A8, 0x24A9, 0x24AA, 0x24AB, 0x24AC, 0x24AD,
          0x24AE, 0x24AF, 0x24B0, 0x24B1, 0x24B2, 0x24B3, 0x24B4, 0x24B5, 0x0030, 0x2474, 0x2475,
          0x2476, 0x2477, 0x2478, 0x2479, 0x2480, 0x2481, 0x2482
      },
      {
          0x1D43, 0x1D47, 0x1D9C, 0x1D48, 0x1D49, 0x1DA0, 0x1D4D, 0x2B0, 0x1DA4, 0x2B2, 0x1D4F,
          0x2E1,
          0x1D50, 0x1DAF, 0x1D52, 0x1D56, 0x1DA3, 0x2B3, 0x2E2, 0x1D57, 0x1D58, 0x1D5B, 0x2B7,
          0x2E3,
          0x2B8, 0x1DBB, 0x1D43, 0x1D47, 0x1D9C, 0x1D48, 0x1D49, 0x1DA0, 0x1D4D, 0x2B0, 0x1DA4,
          0x2B2,
          0x1D4F, 0x2E1, 0x1D50, 0x1DAF, 0x1D52, 0x1D56, 0x1DA3, 0x2B3, 0x2E2, 0x1D57, 0x1D58,
          0x1D5B,
          0x2B7, 0x2E3, 0x2B8, 0x1DBB, 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39
      }
  };

  // Uppercase Lookup for LEET
  private static HashMap<Integer, String> LeetMap = new HashMap<>();
  // Custom probability for rarely used LEET replacements
  private static HashMap<Integer, Integer> LeetProbability = new HashMap<>();

  static {
    LeetMap.put(65, "4");
    LeetMap.put(69, "3");
    LeetMap.put(73, "1");
    LeetProbability.put(73, 60);
    LeetMap.put(76, "1");
    LeetMap.put(79, "0");
    LeetMap.put(83, "5");
    LeetMap.put(84, "7");
    LeetMap.put(77, "|\\/|");
    LeetProbability.put(77, 15);
    LeetMap.put(78, "|\\|");
    LeetProbability.put(78, 20);
    LeetMap.put(66, "8");
    LeetProbability.put(66, 20);
    LeetMap.put(67, "k");
    LeetProbability.put(67, 80);
    LeetMap.put(68, "|)");
    LeetProbability.put(68, 40);
    LeetMap.put(71, "9");
    LeetProbability.put(71, 20);
    LeetMap.put(72, "|-|");
    LeetProbability.put(72, 40);
    LeetMap.put(75, "|<");
    LeetProbability.put(75, 40);
    LeetMap.put(80, "|2");
    LeetProbability.put(80, 20);
    LeetMap.put(85, "|_|");
    LeetProbability.put(85, 20);
    LeetMap.put(86, "\\/");
    LeetProbability.put(86, 40);
    LeetMap.put(87, "\\/\\/");
    LeetProbability.put(87, 30);
    LeetMap.put(88, "><");
    LeetProbability.put(88, 50);
  }

  private final EnumSetting<Mode> font = newEnumSetting(Mode.class)
      .name("font")
      .description("Font to use")
      .defaultTo(Mode.FULL_WIDTH)
      .build();

  private final PatternSetting prefixRegexp = newPatternSetting()
      .name("prefix")
      .description("Command prefixes (RegExp)")
      .defaultTo(Pattern.compile("(^/|//|\\!)(\\w+)"))
      .build();

  private final PatternSetting command0ArgRegexp = newPatternSetting()
      .name("command0Arg")
      .description("Commands where all text may be changed (RegExp)")
      .defaultTo(Pattern.compile("(r|reply)"))
      .build();

  private final PatternSetting command1ArgRegexp = newPatternSetting()
      .name("command1Arg")
      .description("Commands where only the first argument may not be changed (RegExp)")
      .defaultTo(Pattern.compile("(pm|tell|msg|message|w|whisper|nick|mail)"))
      .build();

  @SubscribeListener
  public void onPacketSent(PacketOutboundEvent event) {
    if (event.getPacket() instanceof CChatMessagePacket) {

      boolean is0Arg = false;
      boolean is1Arg = false;
      boolean isIgnore = false;

      String prefix = "";
      String command = "";
      String message;
      String arg1 = "";

      String inputMessage = ((CChatMessagePacket) event.getPacket()).getMessage();

      Matcher prefixMatcher = prefixRegexp.getValue().matcher(inputMessage);
      if (prefixMatcher.find()) {
        prefix = prefixMatcher.group(1);
        command = prefixMatcher.group(2);

        Matcher cmd0ArgMatcher = command0ArgRegexp.getValue().matcher(command);
        Matcher cmd1ArgMatcher = command1ArgRegexp.getValue().matcher(command);

        // if command is found, make sure the match is not just a substring
        if (cmd0ArgMatcher.find() && command.length() == cmd0ArgMatcher.group().length()) {
          is0Arg = true;
          message = inputMessage.substring(prefixMatcher.end());

        } else if (cmd1ArgMatcher.find() && command.length() == cmd1ArgMatcher.group().length()) {
          is1Arg = true;
          Matcher arg1Matcher = Pattern.compile(" .+? ").matcher(inputMessage);

          if (arg1Matcher.find()) {
            arg1 = inputMessage.substring(arg1Matcher.start(), arg1Matcher.end()).trim();
            message = inputMessage.substring(arg1Matcher.end());
          } else {
            isIgnore = true;
            message = inputMessage;
          }

        } else {
          message = inputMessage;
          // Completely ignore all unknown commands
          isIgnore = true;
        }
      } else {
        message = inputMessage;
      }

      if (!isIgnore) {
        String messageOut = prettify(message);

        if (is0Arg) {
          messageOut = prefix + command + " " + messageOut;
        } else if (is1Arg) {
          messageOut = prefix + command + " " + arg1 + " " + messageOut;
        }

        if (Common.getNetworkManager() != null) {
          CChatMessagePacket packet = new CChatMessagePacket(messageOut);
          Common.getNetworkManager().dispatchSilentNetworkPacket(packet);
          event.setCanceled(true);
        }
      }
    }
  }

  private String makeLeet(String message) {
    char[] messageArray;

    message = message.replaceAll("(?i)dude", "d00d").replaceAll("(^|\\s)ph", "$1f");

    messageArray = message.toCharArray();
    // match and replace the last only S in a word
    Matcher zMatcher = Pattern.compile("(?<![sS])([sS])(?:[^\\w]|$)").matcher(message);

    while (!zMatcher.hitEnd()) {
      if (zMatcher.find()) {
        if (zMatcher.group(1).equals("s")) {
          messageArray[zMatcher.end(1) - 1] = 'z';
        } else {
          messageArray[zMatcher.end(1) - 1] = 'Z';
        }
      }
    }

    StringBuilder builder = new StringBuilder();
    Random random = new Random();
    for (char c : messageArray) {
      int key = Character.toUpperCase(c);
      // half the probability for LEET
      if (random.nextInt(2) == 0
          && LeetMap.get(key) != null
          && (LeetProbability.get(key) == null
          || LeetProbability.get(key) > random.nextInt(100))) {
        builder.append(LeetMap.get(key));
      } else {
        builder.append(c);
      }
    }

    return builder.toString();
  }

  private String makeWave(String message) {
    char[] messageArray = message.toCharArray();
    ThreadLocalRandom rand = ThreadLocalRandom.current();
    double span = rand.nextDouble(0.4D, 1.3D);
    double xoff = rand.nextDouble(0, 32);
    double yoff = rand.nextDouble(-0.4, 0.6);

    for (int i = 0; i < messageArray.length; i++) {
      if (waveCharIsUpper(i, span, xoff, yoff)) {
        messageArray[i] = Character.toUpperCase(messageArray[i]);
      }
    }
    return new String(messageArray);
  }

  private String randomCase(String message) {
    char[] messageArray = message.toCharArray();
    ThreadLocalRandom rand = ThreadLocalRandom.current();

    for (int i = 0; i < messageArray.length; i++) {
      if (rand.nextBoolean()) {
        messageArray[i] = Character.toUpperCase(messageArray[i]);
      } else {
        messageArray[i] = Character.toLowerCase(messageArray[i]);
      }
    }
    return new String(messageArray);
  }

  private boolean waveCharIsUpper(double x, double span, double xoff, double yoff) {
    // defaults: span = 0.4, xoff=0, yoff=-0.5
    return Math.sin(x * span + xoff) + yoff > 0;
  }

  private String changeAlphabet(String message, Mode fontType) {
    char[] messageArray = message.toCharArray();
    int[] currentFont = FONT[fontType.ordinal()];
    int i = 0;

    for (char c : messageArray) {
      int letterKey = alphabet.indexOf(c);
      if (letterKey != -1 && (c != (char) 0x3E)) {
        messageArray[i] = (char) currentFont[letterKey];
      }
      i++;
    }
    return new String(messageArray);
  }

  public String prettify(String message) {
    switch (font.getValue()) {
      case LEET:
        return makeLeet(message);
      case WAVE:
        return makeWave(message);
      case RANDOMCASE:
        return randomCase(message);
      default:
        return changeAlphabet(message, font.getValue());
    }
  }
}
