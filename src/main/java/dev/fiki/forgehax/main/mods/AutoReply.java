package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.cmd.settings.StringSetting;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import joptsimple.internal.Strings;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod(
    name = "AutoReply",
    description = "Automatically talk in chat if finds a strings",
    category = Category.MISC
)
public class AutoReply extends ToggleMod {
  public final StringSetting reply = newStringSetting()
      .name("reply")
      .description("Text to reply with")
      .defaultTo("fuck off newfag")
      .build();

  public final StringSetting mode = newStringSetting()
      .name("mode")
      .description("Reply or chat")
      .defaultTo("REPLY")
      .build();

  public final StringSetting search = newStringSetting()
      .name("search")
      .description("Text to search for in message")
      .defaultTo("whispers: ")
      .build();

  @SubscribeEvent
  public void onClientChat(ClientChatReceivedEvent event) {
    String message = (event.getMessage().getUnformattedComponentText());
    if (message.contains(search.getValue()) && !message.startsWith(Common.MC.getSession().getUsername())) {
      String append;
      switch (mode.getValue().toUpperCase()) {
        case "REPLY":
          append = "/r ";
          break;
        case "CHAT":
        default:
          append = Strings.EMPTY;
          break;
      }
      Common.getLocalPlayer().sendChatMessage(append + reply.getValue());
    }
  }
}
