package dev.fiki.forgehax.main.mods.commands;

import com.google.common.util.concurrent.FutureCallback;
import com.mojang.authlib.GameProfile;
import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.cmd.AbstractCommand;
import dev.fiki.forgehax.main.util.cmd.ICommand;
import dev.fiki.forgehax.main.util.cmd.argument.Arguments;
import dev.fiki.forgehax.main.util.cmd.flag.EnumFlag;
import dev.fiki.forgehax.main.util.cmd.value.IValue;
import dev.fiki.forgehax.main.util.mod.CommandMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import joptsimple.internal.Strings;
import net.minecraft.client.network.play.NetworkPlayerInfo;

import static dev.fiki.forgehax.main.Common.*;

/**
 * Created on 6/1/2017 by fr1kin
 */
@RegisterMod
public class HelpCommand extends CommandMod {

  public HelpCommand() {
    super("HelpCommand");
  }

  {
    newSimpleCommand()
        .name("save")
        .description("Save all configurations")
        .flag(EnumFlag.EXECUTOR_ASYNC)
        .executor(args -> getRootCommand().serialize())
        .build();
  }

  {
    newSimpleCommand()
        .name("help")
        .description("Help text for mod syntax and command list")
        .executor(args -> {
          args.inform("Type \".search <optional: containing string>\" for list of mods");
          args.inform("Use -? or --help after command to see command options");
          args.inform("See the FAQ for details");
          args.inform("https://github.com/fr1kin/ForgeHax#faq");
        })
        .build();
  }

  {
    newSimpleCommand()
        .name("find")
        .description("Lists all the mods or all the mods containing the given argument")
        .argument(Arguments.newStringArgument()
            .label("mod")
            .build())
        .executor(args -> {
          IValue<String> modSearch = args.getFirst();
          args.inform(getRootCommand().getPossibleMatchingChildren(modSearch.getValue()).stream()
              .map(ICommand::getName)
              .collect(Collectors.joining(", ")));
        })
        .build();
  }

  {
    newSimpleCommand()
        .name("history")
        .description("Lists name history of given player")
        .argument(Arguments.newStringArgument()
            .label("name")
            .build())
        .executor(args -> {
          // TODO: 1.15
        })
        .build();
  }

  {
    newSimpleCommand()
        .name("loaded")
        .description("Loaded plugin list")
        .executor(args -> {
          args.inform(getModManager().getMods().stream()
              .map(AbstractCommand::getName)
              .sorted(String.CASE_INSENSITIVE_ORDER)
              .collect(Collectors.joining(", ")));
        })
        .build();
  }

  {
    newSimpleCommand()
        .name("players-online")
        .description("List of online players. Optionally with an argument to match")
        .flag(EnumFlag.EXECUTOR_MAIN_THREAD)
        .executor(args -> {
          if (MC.getConnection() == null) {
            args.error("Not connected to a server.");
            return;
          }

          args.inform(MC.getConnection().getPlayerInfoMap().stream()
              .map(NetworkPlayerInfo::getGameProfile)
              .map(GameProfile::getName)
              .collect(Collectors.joining(", ")));
        })
        .build();
  }

  {
    newSimpleCommand()
        .name("respawn")
        .description("Send respawn packet")
        .flag(EnumFlag.EXECUTOR_MAIN_THREAD)
        .executor(args -> {
          if (getLocalPlayer() != null) {
            getLocalPlayer().respawnPlayer();
            args.inform("Respawn packet sent");
          } else {
            args.error("Failed to send respawn packet (player is null)");
          }
        })
        .build();
  }

  {
    newSimpleCommand()
        .name("chat-clear")
        .description("Clears chat")
        .flag(EnumFlag.EXECUTOR_MAIN_THREAD)
        .executor(args -> MC.ingameGUI.getChatGUI().clearChatMessages(true))
        .build();
  }
}
