package dev.fiki.forgehax.main.commands;

import dev.fiki.forgehax.api.cmd.argument.Arguments;
import dev.fiki.forgehax.api.extension.GeneralEx;
import dev.fiki.forgehax.api.mod.CommandMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import lombok.experimental.ExtensionMethod;
import net.minecraft.network.play.client.CChatMessagePacket;

import static dev.fiki.forgehax.main.Common.getNetworkManager;

@RegisterMod
@ExtensionMethod({GeneralEx.class})
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
          getNetworkManager().dispatchSilentNetworkPacket(new CChatMessagePacket(msg));
        })
        .build();
  }
}
