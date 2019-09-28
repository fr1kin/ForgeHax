package com.matt.forgehax.mods.services;

import com.matt.forgehax.Helper;
import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.util.PacketHelper;
import com.matt.forgehax.util.command.CommandHelper;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.command.exception.CommandExecuteException;
import com.matt.forgehax.util.console.ConsoleIO;
import com.matt.forgehax.util.mod.ServiceMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created on 5/15/2017 by fr1kin
 */
@RegisterMod
public class ChatCommandService extends ServiceMod {
  
  private static Character ACTIVATION_CHARACTER = '.';
  
  public static Character getActivationCharacter() {
    return ACTIVATION_CHARACTER;
  }
  
  public final Setting<Character> activationCharacter =
      getCommandStub()
          .builders()
          .<Character>newSettingBuilder()
          .name("activation_char")
          .description("Activation character")
          .defaultTo('.')
          .changed(cb -> ACTIVATION_CHARACTER = cb.getTo())
          .build();
  
  public ChatCommandService() {
    super("ChatCommandService", "Listeners for activation key in chat messages typed");
  }
  
  @Override
  protected void onLoad() {
    ACTIVATION_CHARACTER = activationCharacter.get();
  }
  
  @SubscribeEvent
  public void onSendPacket(PacketEvent.Outgoing.Pre event) {
    if (event.getPacket() instanceof CPacketChatMessage) {
      String message = ((CPacketChatMessage) event.getPacket()).getMessage();
      if (!PacketHelper.isIgnored(event.getPacket())
          && message.startsWith(activationCharacter.getAsString()) && message.length() > 1) {
        // cut out the . from the message
        String line = message.substring(1);
        handleCommand(line);
        event.setCanceled(true);
      }
    }
  }
  
  // to be called from MainMenuGuiService
  public static void handleCommand(String message) {
    ConsoleIO.start();
    ConsoleIO.write(message, ConsoleIO.HEADING);
    ConsoleIO.incrementIndent();
    try {
      String[] arguments = CommandHelper.translate(message);
      GLOBAL_COMMAND.run(arguments);
    } catch (Throwable t) {
      if (!(t instanceof CommandExecuteException)) {
        t.printStackTrace();
      }
      Helper.printMessage(t.getMessage());
    }
    ConsoleIO.finished();
  }
}
