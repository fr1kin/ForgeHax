package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getNetworkManager;

import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.util.PacketHelper;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

// made by BABBAJ

@RegisterMod
public class FancyChat extends ToggleMod {
  private static final String[] MODE = {
    "FULL WIDTH", "CIRCLE", "(((PARENTHESES)))", "SMALL",
  };
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
    {
      0x24B6, 0x24B7, 0x24B8, 0x24B9, 0x24BA, 0x24BB, 0x24BC, 0x24BD, 0x24BE, 0x24BF, 0x24C0,
      0x24C1, 0x24C2, 0x24C3, 0x24C4, 0x24C5, 0x24C6, 0x24C7, 0x24C8, 0x24C9, 0x24CA, 0x24CB,
      0x24CC, 0x24CD, 0x24CE, 0x24CF, 0x24D0, 0x24D1, 0x24D2, 0x24D3, 0x24D4, 0x24D5, 0x24D6,
      0x24D7, 0x24D8, 0x24D9, 0x24DA, 0x24DB, 0x24DC, 0x24DD, 0x24DE, 0x24DF, 0x24E0, 0x24E1,
      0x24E2, 0x24E3, 0x24E4, 0x24E5, 0x24E6, 0x24E7, 0x24E8, 0x24E9, 0x24EA, 0x2460, 0x2461,
      0x2462, 0x2463, 0x2464, 0x2465, 0x2466, 0x2467, 0x2468
    },
    {
      0x249C, 0x249D, 0x249E, 0x249F, 0x24A0, 0x24A1, 0x24A2, 0x24A3, 0x24A4, 0x24A5, 0x24A6,
      0x24A7, 0x24A8, 0x24A9, 0x24AA, 0x24AB, 0x24AC, 0x24AD, 0x24AE, 0x24AF, 0x24B0, 0x24B1,
      0x24B2, 0x24B3, 0x24B4, 0x24B5, 0x249C, 0x249D, 0x249E, 0x249F, 0x24A0, 0x24A1, 0x24A2,
      0x24A3, 0x24A4, 0x24A5, 0x24A6, 0x24A7, 0x24A8, 0x24A9, 0x24AA, 0x24AB, 0x24AC, 0x24AD,
      0x24AE, 0x24AF, 0x24B0, 0x24B1, 0x24B2, 0x24B3, 0x24B4, 0x24B5, 0x30, 0x31, 0x32, 0x33, 0x34,
      0x35, 0x36, 0x37, 0x38, 0x39
    },
    {
      0x1D43, 0x1D47, 0x1D9C, 0x1D48, 0x1D49, 0x1DA0, 0x1D4D, 0x2B0, 0x1DA4, 0x2B2, 0x1D4F, 0x2E1,
      0x1D50, 0x1DAF, 0x1D52, 0x1D56, 0x1DA3, 0x2B3, 0x2E2, 0x1D57, 0x1D58, 0x1D5B, 0x2B7, 0x2E3,
      0x2B8, 0x1DBB, 0x1D43, 0x1D47, 0x1D9C, 0x1D48, 0x1D49, 0x1DA0, 0x1D4D, 0x2B0, 0x1DA4, 0x2B2,
      0x1D4F, 0x2E1, 0x1D50, 0x1DAF, 0x1D52, 0x1D56, 0x1DA3, 0x2B3, 0x2E2, 0x1D57, 0x1D58, 0x1D5B,
      0x2B7, 0x2E3, 0x2B8, 0x1DBB, 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39
    }
  };

  public final Setting<String> font =
      getCommandStub()
          .builders()
          .<String>newSettingBuilder()
          .name("font")
          .description("Font to use")
          .defaultTo(MODE[0])
          .build();

  private String inputMessage = "";
  private String message = "";
  private String recipient = "";
  private Boolean isWhisper = false;
  private Boolean isPM = false;
  private Boolean isIgnore = false;
  private int fontMode;
  // [0] = full width  [1] = circles  [2] = parentheses [3] = small

  public FancyChat() {
    super(Category.MISC, "FancyChat", false, "meme text");
  }

  @SubscribeEvent
  public void onPacketSent(PacketEvent.Outgoing.Pre event) {
    if (event.getPacket() instanceof CPacketChatMessage
        && !PacketHelper.isIgnored(event.getPacket())) {

      inputMessage = ((CPacketChatMessage) event.getPacket()).getMessage();

      for (int i = 0; i < MODE.length; i++) {
        if (font.get().toUpperCase().equals(MODE[i])) {
          fontMode = i;
        }
      }

      if (inputMessage.startsWith("/r ")) {
        message = inputMessage.substring(3);
        isWhisper = true;
        isPM = false;
      } else if (inputMessage.startsWith("/pm ")) {
        Pattern pattern = Pattern.compile("\\s\\w*\\s");
        Matcher matcher = pattern.matcher(inputMessage);
        if (matcher.find()) {
          recipient = inputMessage.substring(matcher.start(), matcher.end()).trim();
        }
        message = inputMessage.replace("/pm " + recipient + " ", "");
        isPM = true;
        isWhisper = false;
        isIgnore = false;

      } else if (inputMessage.startsWith("/ignore")) {
        isIgnore = true;
        isWhisper = false;
        isPM = false;
      } else {
        message = inputMessage;
        isWhisper = false;
        isPM = false;
        isIgnore = false;
      }

      char out[] = message.toCharArray();

      for (int i = 0; i < message.toCharArray().length; i++) {
        if (alphabet.indexOf(message.charAt(i)) != -1
            && ((message.toCharArray()[i])
                != (char)
                    0x3E)) // indexOf returns -1 if character isnt found  -  check for meme arrow
          //					if (FONT[fontMode].length < alphabet.indexOf(message.charAt(i)))
          out[i] = (char) (FONT[fontMode][(alphabet.indexOf(message.charAt(i)))]);
        else // use normal input character if font doesnt have it
        out[i] = message.toCharArray()[i];
      }

      String messageOut = new String(out);

      if (isWhisper) messageOut = "/r " + messageOut;
      else if (isPM) messageOut = "/pm " + recipient + " " + messageOut;
      else if (isIgnore) messageOut = inputMessage;

      CPacketChatMessage packet = new CPacketChatMessage(messageOut);
      PacketHelper.ignore(packet);
      getNetworkManager().sendPacket(packet);
      event.setCanceled(true);
    }
  }
}
