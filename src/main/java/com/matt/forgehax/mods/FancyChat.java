package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getNetworkManager;

import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.util.PacketHelper;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

// made by BABBAJ

@RegisterMod
public class FancyChat extends ToggleMod {
  private enum MODE {
    FULL_WIDTH,
    CIRCLE,
    PARENTHESES,
    SMALL,
    LEET
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
      0x1D43, 0x1D47, 0x1D9C, 0x1D48, 0x1D49, 0x1DA0, 0x1D4D, 0x2B0, 0x1DA4, 0x2B2, 0x1D4F, 0x2E1,
      0x1D50, 0x1DAF, 0x1D52, 0x1D56, 0x1DA3, 0x2B3, 0x2E2, 0x1D57, 0x1D58, 0x1D5B, 0x2B7, 0x2E3,
      0x2B8, 0x1DBB, 0x1D43, 0x1D47, 0x1D9C, 0x1D48, 0x1D49, 0x1DA0, 0x1D4D, 0x2B0, 0x1DA4, 0x2B2,
      0x1D4F, 0x2E1, 0x1D50, 0x1DAF, 0x1D52, 0x1D56, 0x1DA3, 0x2B3, 0x2E2, 0x1D57, 0x1D58, 0x1D5B,
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
    LeetProbability.put(66, 70);
    LeetMap.put(67, "k");
    LeetProbability.put(67, 80);
    LeetMap.put(68, "|)");
    LeetProbability.put(68, 40);
    LeetMap.put(71, "9");
    LeetProbability.put(71, 70);
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

  private final Setting<MODE> font =
      getCommandStub()
          .builders()
          .<MODE>newSettingEnumBuilder()
          .name("font")
          .description("Font to use")
          .defaultTo(MODE.FULL_WIDTH)
          .build();

  public FancyChat() {
    super(Category.MISC, "FancyChat", false, "meme text");
  }

  @SubscribeEvent
  public void onPacketSent(PacketEvent.Outgoing.Pre event) {
    if (event.getPacket() instanceof CPacketChatMessage
        && !PacketHelper.isIgnored(event.getPacket())) {

      String inputMessage;
      String message;
      String recipient = "";
      boolean isWhisper = false;
      boolean isPM = false;
      boolean isIgnore = false;

      inputMessage = ((CPacketChatMessage) event.getPacket()).getMessage();

      Pattern commandPattern = Pattern.compile("/(.+?)\\s");
      Matcher commandMatcher = commandPattern.matcher(inputMessage);
      String command = commandMatcher.find() ? commandMatcher.group() : null;

      if (command == null) {
        message = inputMessage;
      } else if (command.equals("r") || command.equals("reply")) {
        message = inputMessage.substring(commandMatcher.end());

        isWhisper = true;
      } else if (command.equals("pm")
          || command.equals("tell")
          || command.equals("msg")
          || command.equals("message")
          || command.equals("w")
          || command.equals("whisper")) {

        Pattern pattern = Pattern.compile("\\s\\w+\\s");
        Matcher matcher = pattern.matcher(inputMessage);

        if (matcher.find()) {
          recipient = inputMessage.substring(matcher.start(), matcher.end()).trim();
          message = inputMessage.substring(matcher.end() + 1);
          isPM = true;
        } else {
          isIgnore = true;
          message = inputMessage;
        }
      } else {
        message = inputMessage;
        // Completely ignore all unknown commands
        isIgnore = true;
      }

      String messageOut;
      char[] messageArray;

      switch (font.get()) {
        case LEET:
          message =
              message
                  .replaceAll("(?i)dude", "d00d")
                  .replaceAll("(^|\\s)f", "$1ph")
                  .replaceAll("(^|\\s)ph", "$1f");

          messageArray = message.toCharArray();
          // match and replace the last S in a word
          Matcher zMatcher = Pattern.compile("[^,. :!;?]+([sS])").matcher(message);

          while (!zMatcher.hitEnd()) {
            if (zMatcher.find()) {
              if (zMatcher.group(1).equals("s")) {
                messageArray[zMatcher.end() - 1] = 'z';
              } else {
                messageArray[zMatcher.end() - 1] = 'Z';
              }
            }
          }

          StringBuilder builder = new StringBuilder();
          Random random = new Random();
          for (char c : messageArray) {
            int key = (int) Character.toUpperCase(c);
            if (LeetMap.get(key) != null
                && (LeetProbability.get(key) == null
                    || LeetProbability.get(key) > random.nextInt(100))) {
              builder.append(LeetMap.get(key));
            } else {
              builder.append(c);
            }
          }

          messageOut = builder.toString();
          break;

        default:
          messageArray = message.toCharArray();
          int[] currentFont = FONT[font.get().ordinal()];
          int i = 0;

          for (char c : messageArray) {
            int letterKey = alphabet.indexOf(c);
            if (letterKey != -1 && (c != (char) 0x3E)) {
              messageArray[i] = (char) currentFont[letterKey];
            }
            i++;
          }
          messageOut = new String(messageArray);
      }

      if (isWhisper) messageOut = "/" + command + " " + messageOut;
      else if (isPM) messageOut = "/" + command + " " + recipient + " " + messageOut;
      else if (isIgnore) messageOut = inputMessage;

      if (getNetworkManager() != null) {
        CPacketChatMessage packet = new CPacketChatMessage(messageOut);
        PacketHelper.ignore(packet);
        getNetworkManager().sendPacket(packet);
        event.setCanceled(true);
      }
    }
  }
}
