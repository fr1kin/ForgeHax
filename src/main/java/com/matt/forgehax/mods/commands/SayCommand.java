package com.matt.forgehax.mods.commands;

import static com.matt.forgehax.Helper.getLocalPlayer;

import com.matt.forgehax.util.PacketHelper;
import com.matt.forgehax.util.command.Command;
import com.matt.forgehax.util.command.CommandBuilders;
import com.matt.forgehax.util.mod.CommandMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import java.util.Arrays;
import net.minecraft.network.play.client.CPacketChatMessage;

@RegisterMod
public class SayCommand extends CommandMod {
  
  public SayCommand() {
    super("SayCommand");
  }
  
  @RegisterCommand
  public Command say(CommandBuilders builders) {
    return builders
      .newCommandBuilder()
      .name("say")
      .description("Send chat message")
      .options(
        parser -> {
          parser.acceptsAll(Arrays.asList("fake", "f"),
            "Send a fake message that won't be treated as command");
          parser.acceptsAll(Arrays.asList("local", "l"), "Send message from local chat");
        }
      )
      .processor(
        data -> {
          boolean fake = data.hasOption("fake");
          // any emoji will work until 1.13
          final int fakePrefix = 0x1F921;
          String msg = data.getArgumentCount() > 0 ? data.getArgumentAsString(0) : "";
          
          if (getLocalPlayer() != null) {
            if (fake) {
              msg = new StringBuilder().appendCodePoint(fakePrefix).append(msg).toString();
            }
            if (data.hasOption("local")) {
              getLocalPlayer().sendChatMessage(msg);
            } else {
              PacketHelper.ignoreAndSend(new CPacketChatMessage(msg));
            }
          }
        }
      )
      .build();
  }
}
