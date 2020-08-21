package dev.fiki.forgehax.main.services;

import com.google.common.base.MoreObjects;
import dev.fiki.forgehax.asm.events.packet.PacketOutboundEvent;
import dev.fiki.forgehax.main.ui.ConsoleInterface;
import dev.fiki.forgehax.main.util.PacketHelper;
import dev.fiki.forgehax.main.util.TextComponentBuilder;
import dev.fiki.forgehax.main.util.cmd.execution.CommandExecutor;
import dev.fiki.forgehax.main.util.cmd.settings.CharacterSetting;
import dev.fiki.forgehax.main.util.mod.ServiceMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.play.client.CChatMessagePacket;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.Level;

import static dev.fiki.forgehax.main.Common.getLogger;
import static dev.fiki.forgehax.main.Common.printMessage;

@RegisterMod
@RequiredArgsConstructor
public class ChatCommandService extends ServiceMod {
  private static Character ACTIVATION_CHARACTER = '.';

  public static Character getActivationCharacter() {
    return ACTIVATION_CHARACTER;
  }

  private final ConsoleInterface cli;

  public final CharacterSetting activationCharacter = newCharacterSetting()
      .name("activation-char")
      .description("Activation character")
      .defaultTo('.')
      .changedListener((from, to) -> ACTIVATION_CHARACTER = to)
      .build();

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
        printMessage(TextComponentBuilder.builder()
            .color(TextFormatting.GRAY)
            .text("> ")
            .text(line)
            .italic(true)
            .build());

        CommandExecutor.builder()
            .console(cli)
            .exceptionHandler(((throwable, output) -> {
              output.error(MoreObjects.firstNonNull(throwable.getMessage(), throwable.getClass().getSimpleName()));
              getLogger().catching(Level.ERROR, throwable);
            }))
            .build()
            .runLine(line);

        event.setCanceled(true);
      }
    }
  }
}
