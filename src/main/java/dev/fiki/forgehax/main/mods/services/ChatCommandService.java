package dev.fiki.forgehax.main.mods.services;

import dev.fiki.forgehax.common.events.packet.PacketOutboundEvent;
import dev.fiki.forgehax.main.Globals;
import dev.fiki.forgehax.main.util.command.CommandHelper;
import dev.fiki.forgehax.main.util.command.Setting;
import dev.fiki.forgehax.main.util.command.exception.CommandExecuteException;
import dev.fiki.forgehax.main.util.console.ConsoleIO;
import dev.fiki.forgehax.main.util.mod.ServiceMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.util.PacketHelper;
import net.minecraft.network.play.client.CChatMessagePacket;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
  public void onSendPacket(PacketOutboundEvent event) {
    if (event.getPacket() instanceof CChatMessagePacket) {
      String message = ((CChatMessagePacket) event.getPacket()).getMessage();
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
      Globals.GLOBAL_COMMAND.run(arguments);
    } catch (Throwable t) {
      if (!(t instanceof CommandExecuteException)) {
        t.printStackTrace();
      }
      Globals.printError(t.getMessage());
    }
    ConsoleIO.finished();
  }
}
