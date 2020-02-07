package dev.fiki.forgehax.main.mods.services;

import com.google.common.base.MoreObjects;
import dev.fiki.forgehax.common.events.packet.PacketOutboundEvent;
import dev.fiki.forgehax.main.util.cmd.execution.CommandExecutor;
import dev.fiki.forgehax.main.util.cmd.settings.CharacterSetting;
import dev.fiki.forgehax.main.util.mod.ServiceMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.util.PacketHelper;
import net.minecraft.network.play.client.CChatMessagePacket;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static dev.fiki.forgehax.main.Common.*;

/**
 * Created on 5/15/2017 by fr1kin
 */
@RegisterMod
public class ChatCommandService extends ServiceMod {
  private static Character ACTIVATION_CHARACTER = '.';

  public static Character getActivationCharacter() {
    return ACTIVATION_CHARACTER;
  }

  public final CharacterSetting activationCharacter = newCharacterSetting()
      .name("activation-char")
      .description("Activation character")
      .defaultTo('.')
      .changedListener((from, to) -> ACTIVATION_CHARACTER = to)
      .build();

  public ChatCommandService() {
    super("ChatCommandService", "Listeners for activation key in chat messages typed");
  }

  @Override
  protected void onLoad() {
    ACTIVATION_CHARACTER = activationCharacter.getValue();
  }

  @SubscribeEvent
  public void onSendPacket(PacketOutboundEvent event) {
    if (event.getPacket() instanceof CChatMessagePacket && !PacketHelper.isIgnored(event.getPacket())) {
      String message = ((CChatMessagePacket) event.getPacket()).getMessage();
      if (message.startsWith(activationCharacter.getValue().toString()) && message.length() > 1) {
        // cut out the . from the message
        String line = message.substring(1);
        print(line);
        CommandExecutor.builder()
            .console(getCurrentConsoleOutput())
            .exceptionHandler(((throwable, output) -> {
              output.error(MoreObjects.firstNonNull(throwable.getMessage(), throwable.getClass().getSimpleName()));
              getLogger().debug(throwable, throwable);
            }))
            .build()
            .runLine(line);

        event.setCanceled(true);
      }
    }
  }
}
