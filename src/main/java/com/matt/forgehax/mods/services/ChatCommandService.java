package com.matt.forgehax.mods.services;

import com.matt.forgehax.Helper;
import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.util.command.CommandHelper;
import com.matt.forgehax.util.command.Setting;
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
    private static final Character ACTIVATION_CHARACTER = '.';

    public final Setting<Character> activationCharacter = getCommandStub().builders().<Character>newSettingBuilder()
            .name("activation_char")
            .description("Activation character")
            .defaultTo('.')
            .build();

    public ChatCommandService() {
        super("ChatCommandService", "Listeners for activation key in chat messages typed");
    }

    @SubscribeEvent
    public void onSendPacket(PacketEvent.Outgoing.Pre event) {
        if(event.getPacket() instanceof CPacketChatMessage) {
            String message = ((CPacketChatMessage) event.getPacket()).getMessage();
            if(message.startsWith(activationCharacter.getAsString()) && message.length() > 1) {
                // cut out the . from the message
                String line = message.substring(1);
                ConsoleIO.writeHead(line);
                try {
                    String[] arguments = CommandHelper.translate(line);
                    GLOBAL_COMMAND.run(arguments);
                } catch (Throwable t) {
                    Helper.printMessage(t.getMessage());
                }
                event.setCanceled(true);
            }
        }
    }
}
