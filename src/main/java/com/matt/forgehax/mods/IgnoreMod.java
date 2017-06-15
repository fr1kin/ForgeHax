package com.matt.forgehax.mods;

import com.google.common.collect.Lists;
import com.matt.forgehax.Helper;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
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

    private long lastMessageTime = 0L;
    private Pattern pattern = Pattern.compile("^([\\w*<>]+)");

    private List<String> ignoreList = Lists.newCopyOnWriteArrayList();

    public IgnoreMod() {
        super("IgnoreMod", false, "Clientside ignore");
    }

    private void parseNameFile(File file) {
        if (!file.exists() || !file.isFile()) return;
        ignoreList.clear();
        try {
            Scanner scanner = new Scanner(new FileReader(file));
            scanner.useDelimiter("\r\n");
            while (scanner.hasNext()) {
                String name = scanner.next();
                if (name.length() > 16)
                    name = name.substring(0, 15);
                ignoreList.add(name);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {

        File nameFile = Helper.getFileManager().getFileInConfigDirectory("ignorelist.txt");
        parseNameFile(nameFile);
    }

    @SubscribeEvent
    public void onClientChat(ClientChatReceivedEvent event) {
        String message = (event.getMessage().getUnformattedText());

        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            String messagePlayer = message.substring(matcher.start(), matcher.end()).trim()
                    .replaceAll("<>","").toLowerCase();
            if (ignoreList.contains(messagePlayer) || message.contains(" ur ignored get raped"))
                event.setCanceled(true); // chat message has been (((shut down)))

            if (System.currentTimeMillis() >= lastMessageTime + 1500L)  {
                CPacketChatMessage packet = new CPacketChatMessage("/pm " + messagePlayer + " ur ignored get raped");
                Utils.OUTGOING_PACKET_IGNORE_LIST.add(packet);
                Helper.getNetworkManager().sendPacket(packet);
                lastMessageTime = System.currentTimeMillis();
            }
        }
    }

    @Override
    public void onLoad() {
        GLOBAL_COMMAND.builders().newCommandBuilder()
                .name("ignore")
                .description("Ignore a player")
                .processor(data -> {
                    data.requiredArguments(1);
                    String addName = data.getArgumentAsString(0);
                    // do stuff
                    if (ignoreList.contains((addName))) {
                        ignoreList.remove(ignoreList.indexOf(addName));
                        MC.player.sendMessage(new TextComponentString("\u00A7a" + addName + " has been unignored"));
                    }
                    else {
                        ignoreList.add(addName);
                        MC.player.sendMessage(new TextComponentString("\u00A77" + addName + " has been ignored"));
                    }

                    try {
                        File nameFile = Helper.getFileManager().getFileInConfigDirectory("ignorelist.txt");
                        FileWriter fw = new FileWriter(nameFile);
                        BufferedWriter bw = new BufferedWriter(fw);
                        for (String s : ignoreList) {
                            bw.write(s + "\r\n");
                        }
                        bw.close();
                        fw.close();
                    } catch (Exception e) {
                        ;
                    } finally {
                        data.markSuccess();
                    }
                })
                .build();
    }

}

