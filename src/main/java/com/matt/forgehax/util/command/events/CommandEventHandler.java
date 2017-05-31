package com.matt.forgehax.util.command.events;

import com.matt.forgehax.FileManager;
import com.matt.forgehax.ForgeHax;
import com.matt.forgehax.Globals;
import com.matt.forgehax.Wrapper;
import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.util.command.CommandExecutor;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created on 5/15/2017 by fr1kin
 */
public class CommandEventHandler implements Globals {
    private static final Character ACTIVATION_CHARACTER = '.';

    private static final AtomicBoolean printed = new AtomicBoolean(false);

    private void firstStartupMessage(final File file) {
        if(printed.compareAndSet(false, true)) {
            Wrapper.printMessageNaked("Running ForgeHax version " + ForgeHax.VERSION);
            Wrapper.printMessageNaked("Type .help in chat to learn how to use commands");
            try {
                Files.createFile(file.toPath());
            } catch (IOException e) {
                ;
            }
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        final File file = FileManager.getInstance().getFileInConfigDirectory(".once");
        if(!file.exists()) Executors.newSingleThreadExecutor().execute(() -> {
            while (Wrapper.getWorld() == null) try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                ;
            }
            if(Wrapper.getWorld() != null) MC.addScheduledTask(() -> firstStartupMessage(file));
        });
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

    //
    // ignore this stuff
    //

    private static CommandEventHandler instance = null;

    public static void register() {
        if(instance == null) MinecraftForge.EVENT_BUS.register(instance = new CommandEventHandler());
    }

    public static void unregister() {
        if(instance != null) {
            MinecraftForge.EVENT_BUS.register(instance);
            instance = null;
        }
    }

    public static boolean isRegistered() {
        return instance != null;
    }
}
