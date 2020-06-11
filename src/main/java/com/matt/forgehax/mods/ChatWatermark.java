package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getNetworkManager;
import static com.matt.forgehax.Helper.printError;

import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.util.PacketHelper;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Objects;

// THANKS BABBAJ I'M USING LOTTA STUFF FROM FANCYCHAT

@RegisterMod
public class ChatWatermark extends ToggleMod {

  private final Setting<String> text =
      getCommandStub()
          .builders()
          .<String>newSettingBuilder()
          .name("text")
          .description("Text to add at the end of messages")
          .defaultTo("imagine not using ForgeHax :^)")
          .build();
  
  public ChatWatermark() {
    super(Category.CHAT, "ChatWatermark", false, "Add text after your chat to be ever cringier");
  }
  
  @SubscribeEvent
  public void onPacketSent(PacketEvent.Outgoing.Pre event) {
    if (event.getPacket() instanceof CPacketChatMessage
        && !PacketHelper.isIgnored(event.getPacket())) {
      String inputMessage = ((CPacketChatMessage) event.getPacket()).getMessage();

	  if (!inputMessage.startsWith("/")) {
          CPacketChatMessage packet = new CPacketChatMessage(inputMessage + " | " + text.get());
          PacketHelper.ignore(packet);
          Objects.requireNonNull(getNetworkManager()).sendPacket(packet);
          event.setCanceled(true);
      }
    }
  }
}
