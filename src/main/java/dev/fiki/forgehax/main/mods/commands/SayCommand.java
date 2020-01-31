package dev.fiki.forgehax.main.mods.commands;

import dev.fiki.forgehax.main.Globals;
import dev.fiki.forgehax.main.util.command.Command;
import dev.fiki.forgehax.main.util.command.CommandBuilders;
import dev.fiki.forgehax.main.util.mod.CommandMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.util.PacketHelper;
import net.minecraft.network.play.client.CChatMessagePacket;

import java.util.Arrays;

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
      .options(parser -> {
        parser.acceptsAll(Arrays.asList("fake", "f"), "Send a fake message that won't be treated as command");
        parser.acceptsAll(Arrays.asList("local", "l"), "Send message from local chat");
      })
      .processor(
        data -> {
          boolean fake = data.hasOption("fake");
          // any emoji will work until 1.13
          final int fakePrefix = 0x1F921;
          String msg = data.getArgumentCount() > 0 ? data.getArgumentAsString(0) : "";
          
          if (Globals.getLocalPlayer() != null) {
            if (fake) {
              msg = new StringBuilder().appendCodePoint(fakePrefix).append(msg).toString();
            }
            if (data.hasOption("local")) {
              Globals.getLocalPlayer().sendChatMessage(msg);
            } else {
              PacketHelper.ignoreAndSend(new CChatMessagePacket(msg));
            }
          }
        }
      )
      .build();
  }
}
