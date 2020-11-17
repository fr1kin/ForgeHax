package dev.fiki.forgehax.main.commands;

import dev.fiki.forgehax.api.PacketHelper;
import dev.fiki.forgehax.api.cmd.argument.Arguments;
import dev.fiki.forgehax.api.mod.CommandMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import net.minecraft.network.play.client.CChatMessagePacket;

@RegisterMod
public class SayCommand extends CommandMod {

  {
    newSimpleCommand()
        .name("say")
        .description("Send chat message")
        .argument(Arguments.newStringArgument()
            .label("message")
            .build())
        .executor(args -> {
          String msg = args.getFirst().getStringValue();
          PacketHelper.ignoreAndSend(new CChatMessagePacket(msg));
        })
        .build();
  }
}
