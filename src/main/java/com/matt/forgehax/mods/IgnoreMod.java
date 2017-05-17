package com.matt.forgehax.mods;

import com.google.common.collect.Lists;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.config.Configuration;
import com.matt.forgehax.asm.events.PacketEvent;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraftforge.event.world.WorldEvent;


import java.util.Scanner;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedWriter;

/**
 * Created by Babbaj on 5/10/2017.
 */

@RegisterMod
public class IgnoreMod extends ToggleMod {

    private String message;
    private String messagePlayer;
    private String inputMessage;
    private String addName;

    private List<String> ignoreList = Lists.newCopyOnWriteArrayList();

    public IgnoreMod() {
        super("IgnoreMode", false, "Clientside ignore");
    }

    private void parseNameFile(File file) {
        if(!file.exists() || !file.isFile()) return;
        ignoreList.clear();
        try {
            Scanner scanner = new Scanner(new FileReader(file));
            scanner.useDelimiter("\r\n");
            while(scanner.hasNext()) {
                String name = scanner.next();
                if(name.length() > 16)
                    name = name.substring(0, 15);
                ignoreList.add(name);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        File nameFile = new File(MOD.getBaseDirectory(), "ignorelist.txt");
        parseNameFile(nameFile);
    }

    @SubscribeEvent
    public void onClientChat(ClientChatReceivedEvent event) {
        message = (event.getMessage().getUnformattedText());

        Pattern pattern = Pattern.compile("^([\\w*<>]+)");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            messagePlayer = message.substring(matcher.start(), matcher.end()).trim();
            messagePlayer = messagePlayer.replaceAll("[<>]","").toLowerCase();
            if (ignoreList.contains(messagePlayer))  {
                event.setCanceled(true); // chat message has been (((shut down)))
            }
        }
    }

    @SubscribeEvent // add or remove names to ignorelist
    public void onPacketSent(PacketEvent.Outgoing.Pre event) {
        if (event.getPacket() instanceof CPacketChatMessage) {
            String message = ((CPacketChatMessage) event.getPacket()).getMessage();
            Scanner scanner = new Scanner(message);
            scanner.useDelimiter(" ");
            if (scanner.next().equals(".ignore") && scanner.hasNext()) {
                addName = scanner.next().toLowerCase();
                if (!ignoreList.contains((addName)) ) {
                    ignoreList.add(addName);
                    MC.player.sendMessage(new TextComponentString("\u00A77" + addName +  " has been ignored"));
                }
                else if (ignoreList.contains((addName))) {
                    ignoreList.remove(ignoreList.indexOf(addName));
                    MC.player.sendMessage(new TextComponentString("\u00A7a" + addName +  " has been unignored"));
                }
                event.setCanceled(true);

                try {
                    File nameFile = new File(MOD.getBaseDirectory(), "ignorelist.txt");
                    FileWriter fw = new FileWriter(nameFile);
                    BufferedWriter bw = new BufferedWriter(fw);
                    for (String s : ignoreList) {
                        bw.write(s + "\r\n");
                    }
                    bw.close();
                    fw.close();
                }
                catch(Exception e) {
                }
            }
            scanner.close();
        }
    }
}
