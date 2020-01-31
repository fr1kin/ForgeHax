package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.Globals;
import dev.fiki.forgehax.main.util.command.Setting;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import joptsimple.internal.Strings;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod
public class AutoReply extends ToggleMod {
  
  public final Setting<String> reply =
      getCommandStub()
          .builders()
          .<String>newSettingBuilder()
          .name("reply")
          .description("Text to reply with")
          .defaultTo("fuck off newfag")
          .build();
  
  public final Setting<String> mode =
      getCommandStub()
          .builders()
          .<String>newSettingBuilder()
          .name("mode")
          .description("Reply or chat")
          .defaultTo("REPLY")
          .build();
  
  public final Setting<String> search =
      getCommandStub()
          .builders()
          .<String>newSettingBuilder()
          .name("search")
          .description("Text to search for in message")
          .defaultTo("whispers: ")
          .build();
  
  public AutoReply() {
    super(Category.MISC, "AutoReply", false, "Automatically talk in chat if finds a strings");
  }
  
  @SubscribeEvent
  public void onClientChat(ClientChatReceivedEvent event) {
    String message = (event.getMessage().getUnformattedComponentText());
    if (message.contains(search.get()) && !message.startsWith(Globals.MC.getSession().getUsername())) {
      String append;
      switch (mode.get().toUpperCase()) {
        case "REPLY":
          append = "/r ";
          break;
        case "CHAT":
        default:
          append = Strings.EMPTY;
          break;
      }
      Globals.getLocalPlayer().sendChatMessage(append + reply.get());
    }
  }
}
