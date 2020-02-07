package dev.fiki.forgehax.main.mods.commands;

import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.cmd.argument.Arguments;
import dev.fiki.forgehax.main.util.cmd.flag.EnumFlag;
import dev.fiki.forgehax.main.util.mod.CommandMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.util.PacketHelper;
import net.minecraft.network.play.client.CChatMessagePacket;

import java.util.Arrays;

import static dev.fiki.forgehax.main.Common.*;

@RegisterMod
public class SayCommand extends CommandMod {

  public SayCommand() {
    super("SayCommand");
  }

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
