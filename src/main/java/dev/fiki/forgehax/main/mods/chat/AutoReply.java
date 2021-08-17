package dev.fiki.forgehax.main.mods.chat;

import dev.fiki.forgehax.api.cmd.settings.StringSetting;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import joptsimple.internal.Strings;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import static dev.fiki.forgehax.main.Common.getLocalPlayer;

@RegisterMod(
    name = "AutoReply",
    description = "Automatically talk in chat if finds a strings",
    category = Category.CHAT
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

  @SubscribeListener
  public void onClientChat(ClientChatReceivedEvent event) {
    String message = (event.getMessage().getString());
    if (message.contains(search.getValue()) && !message.startsWith(MC.getUser().getName())) {
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
      getLocalPlayer().chat(append + reply.getValue());
    }
  }
}
