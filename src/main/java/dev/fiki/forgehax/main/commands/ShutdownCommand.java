package dev.fiki.forgehax.main.commands;

import dev.fiki.forgehax.api.cmd.flag.EnumFlag;
import dev.fiki.forgehax.api.mod.CommandMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import net.minecraft.util.text.StringTextComponent;

import java.util.Objects;

import static dev.fiki.forgehax.main.Common.getNetworkManager;
import static dev.fiki.forgehax.main.Common.isInWorld;

/**
 * Added by OverFloyd - march 10, 2021
 */
@RegisterMod
public class ShutdownCommand extends CommandMod {

  // TODO: also add disconnect cmd without the game crashing
  {
    newSimpleCommand()
        .name("shutdown")
        .description("Closes the client")
        .flag(EnumFlag.EXECUTOR_MAIN_THREAD)
        .executor(args -> {
          args.inform("Shutting down...");
          //getRootCommand().writeConfiguration(); // doesn't work for some reasons

          if (!MC.isSingleplayer() && isInWorld()) {
            Objects.requireNonNull(getNetworkManager()).closeChannel(new StringTextComponent("Shutting down"));
          }

          MC.shutdown();
        })
        .build();
  }
}
