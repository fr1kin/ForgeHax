package com.matt.forgehax.mods.core;

import com.matt.forgehax.FileManager;
import com.matt.forgehax.ForgeHax;
import com.matt.forgehax.Globals;
import com.matt.forgehax.Wrapper;
import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.command.CommandExecutor;
import com.matt.forgehax.util.mod.SilentListenerMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created on 5/15/2017 by fr1kin
 */
@RegisterMod
public class CommandListener extends SilentListenerMod {
    private static final Character ACTIVATION_CHARACTER = '.';
    private static final File STARTUP_ONCE = FileManager.getInstance().getFileInConfigDirectory(".once");

    private boolean finished = false;

    public CommandListener() {
        super("CommandListener", "Listeners for activation key in chat messages typed");
    }

    @SubscribeEvent
    public void onSendPacket(PacketEvent.Outgoing.Pre event) {
        if(event.getPacket() instanceof CPacketChatMessage) {
            String message = ((CPacketChatMessage) event.getPacket()).getMessage();
            if(message.startsWith(ACTIVATION_CHARACTER.toString()) && message.length() > 1) {
                // cut out the . from the message
                String line = message.substring(1);
                CommandExecutor.run(line);
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
        if(!finished) {
            Path path = STARTUP_ONCE.toPath();
            // read files last version
            String version = "";
            if(STARTUP_ONCE.exists()) try {
                version = new String(Files.readAllBytes(path));
            } catch (Exception e) {}
            if(!Objects.equals(ForgeHax.VERSION, version)) {
                Wrapper.printMessageNaked(ForgeHax.getWelcomeMessage());
                try {
                    Files.write(path, ForgeHax.VERSION.getBytes());
                } catch (IOException e) {
                    ;
                }
            }
            finished = true;
        }
    }
}
