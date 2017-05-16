package com.matt.forgehax;

import com.google.common.base.Strings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.NetworkManager;
import net.minecraft.util.text.TextComponentSelector;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.apache.logging.log4j.Logger;

import java.util.Scanner;

/**
 * Created on 4/25/2017 by fr1kin
 */
public class Wrapper implements Globals {
    public static Minecraft getMinecraft() {
        return MC;
    }

    public static ForgeHax getMod() {
        return MOD;
    }

    public static Logger getLog() {
        return MOD.log;
    }

    public static EntityPlayerSP getLocalPlayer() {
        return MC.player;
    }

    public static World getWorld() {
        return MC.world;
    }

    public static void printMessageNaked(String message, boolean makeGrey) {
        if(getLocalPlayer() != null && !Strings.isNullOrEmpty(message)) {
            if(message.contains("\n")) {
                Scanner scanner = new Scanner(message);
                scanner.useDelimiter("\n");
                makeGrey = !makeGrey; // so it starts with white
                while (scanner.hasNext()) printMessageNaked(scanner.next(), makeGrey = !makeGrey);
            } else {
                TextComponentString string = new TextComponentString(message.replaceAll("\r", ""));
                if(makeGrey) string.getStyle().setColor(TextFormatting.GRAY);
                getLocalPlayer().sendMessage(string);
            }
        }
    }

    public static void printMessageNaked(String message) {
        printMessageNaked(message, false);
    }

    // Will append '[FH] ' in front
    public static void printMessage(String message) {
        if(!Strings.isNullOrEmpty(message)) printMessageNaked("[FH] " + message);
    }

    public static NetworkManager getNetworkManager() {
        return FMLClientHandler.instance().getClientToServerNetworkManager();
    }
}
