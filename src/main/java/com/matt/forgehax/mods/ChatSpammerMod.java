package com.matt.forgehax.mods;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.matt.forgehax.asm.events.PacketEvent;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

public class ChatSpammerMod extends ToggleMod {
    public Property delay;

    private int index = 0;
    private long timeLastMessageSent = -1;

    private final List<String> spamList = Lists.newCopyOnWriteArrayList();

    public ChatSpammerMod(String modName, boolean defaultValue, String description, int key) {
        super(modName, defaultValue, description, key);
    }

    private void parseSpamFile(File file) {
        if(!file.exists() || !file.isFile()) return;
        spamList.clear();
        try {
            Scanner scanner = new Scanner(new FileReader(file));
            scanner.useDelimiter("\n");
            while(scanner.hasNext()) {
                String msg = scanner.next();
                if(msg.length() > 240)
                    msg = msg.substring(0, 239);
                spamList.add(msg);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEnabled() {
        index = 0;
        timeLastMessageSent = -1;
        File spamFile = new File(MOD.getBaseDirectory(), "spam.txt");
        parseSpamFile(spamFile);
    }

    @Override
    public void loadConfig(Configuration configuration) {
        addSettings(
                delay = configuration.get(getModName(),
                        "delay",
                        5000,
                        "Delay between messages in ms"
                )
        );
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if(MC.thePlayer == null) return;
        switch (event.phase) {
            case START:
                break;
            case END:
            {
                if(System.currentTimeMillis() >= timeLastMessageSent + delay.getInt()) {
                    try {
                        //TODO: maybe use a double linked list
                        MC.thePlayer.sendChatMessage(spamList.get(index % spamList.size()));
                        timeLastMessageSent = System.currentTimeMillis();
                    } catch(Exception e) {
                        MOD.printStackTrace(e);
                    } finally {
                        index++;
                    }
                }
                break;
            }
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send.Pre event) {
        if(event.getPacket() instanceof CPacketChatMessage) {
            timeLastMessageSent = System.currentTimeMillis();
        }
    }
}
