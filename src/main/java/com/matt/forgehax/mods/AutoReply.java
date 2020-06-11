package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;

import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import joptsimple.internal.Strings;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.Display;

@RegisterMod
public class AutoReply extends ToggleMod {

  public AutoReply() {
    super(Category.MISC, "AutoReply", false, "Automatically talk in chat if finds a strings");
  }

  public final Setting<String> text =
      getCommandStub()
          .builders()
          .<String>newSettingBuilder()
          .name("text")
          .description("Text to reply with")
          .defaultTo("fuck off newfag")
          .build();

  public enum DetectionMode {
    CHAT,
    REPLY
  }

  public final Setting<DetectionMode> mode =
      getCommandStub()
          .builders()
          .<DetectionMode>newSettingEnumBuilder()
          .name("mode")
          .description("Detection mode")
          .defaultTo(DetectionMode.REPLY)
          .build();

  public final Setting<String> search =
      getCommandStub()
          .builders()
          .<String>newSettingBuilder()
          .name("search")
          .description("Text to search for in message")
          .defaultTo("whispers: ")
          .build();

  private final Setting<Boolean> onLostFocus =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("no-focus")
          .description("Makes the mod only work on lost focus")
          .defaultTo(false)
          .build();

  @SubscribeEvent
  public void onClientChat(ClientChatReceivedEvent event) {
    String message = event.getMessage().getUnformattedText();
    if (message.contains(search.get()) && !message.startsWith(MC.getSession().getUsername())) {
      String append;
      switch (mode.get()) {
        case REPLY:
          append = "/r ";
          break;
        case CHAT:
        default:
          append = Strings.EMPTY;
          break;
      }
      if (onLostFocus.getAsBoolean() && !Display.isActive()) {
        getLocalPlayer().sendChatMessage(append + text.get());
      } else if (!onLostFocus.getAsBoolean()){
        getLocalPlayer().sendChatMessage(append + text.get());
      }
    }
  }

  @Override
  public String getDebugDisplayText() {
    switch (mode.get()){
      case CHAT:{
        return String.format("%s [C]", super.getDisplayText());
      }
      case REPLY: {
        return String.format("%s [R]", super.getDisplayText());
      }
      default: return String.format("%s", super.getDisplayText());
    }
  }
}
