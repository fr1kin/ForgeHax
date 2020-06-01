package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;

import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import joptsimple.internal.Strings;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

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
    super(Category.CHAT, "AutoReply", false, "Automatically talk in chat if finds a strings");
  }
  
  @SubscribeEvent
  public void onClientChat(ClientChatReceivedEvent event) {
    String message = (event.getMessage().getUnformattedText());
    if (message.matches(search.get()) && !message.startsWith(MC.getSession().getUsername())) {
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
      getLocalPlayer().sendChatMessage(append + reply.get());
    }
  }
}
