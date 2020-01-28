package com.matt.forgehax;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.matt.forgehax.util.FileManager;
import com.matt.forgehax.util.command.CommandGlobal;
import com.matt.forgehax.util.mod.loader.ModManager;
import java.util.Optional;
import java.util.Scanner;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.NetworkManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import org.apache.logging.log4j.Logger;

/**
 * Created on 4/25/2017 by fr1kin
 */
@Deprecated
public class Helper implements Globals {

  @Deprecated
  public static CommandGlobal getGlobalCommand() {
    return CommandGlobal.getInstance();
  }

  @Deprecated
  public static Minecraft getMinecraft() {
    return MC;
  }

  @Deprecated
  public static ModManager getModManager() {
    return ModManager.getInstance();
  }

  @Deprecated
  public static FileManager getFileManager() {
    return FileManager.getInstance();
  }

  @Deprecated
  public static Logger getLog() {
    return LOGGER;
  }

  @Deprecated
  public static ClientPlayerEntity getLocalPlayer() {
    return MC.player;
  }

  @Deprecated
  @Nullable
  public static Entity getRidingEntity() {
    return getLocalPlayer() == null ? null : getLocalPlayer().getRidingEntity();
  }

  @Deprecated
  // Returns the riding entity if present, otherwise the local player
  @Nullable
  public static Entity getRidingOrPlayer() {
    return Optional.ofNullable(getRidingEntity()).orElse(getLocalPlayer());
  }

  @Deprecated
  @Nullable
  public static ClientWorld getWorld() {
    return MC.world;
  }

  @Deprecated
  public static World getWorld(Entity entity) {
    return entity.getEntityWorld();
  }

  @Deprecated
  public static World getWorld(TileEntity tileEntity) {
    return tileEntity.getWorld();
  }

  @Deprecated
  @Nullable
  public static NetworkManager getNetworkManager() {
    return MC.getConnection() != null ? MC.getConnection().getNetworkManager() : null;
  }

  @Deprecated
  public static PlayerController getPlayerController() {
    return MC.playerController;
  }

  @Deprecated
  public static void printMessageNaked(
      String startWith, String message, Style firstStyle, Style secondStyle) {
    if (!Strings.isNullOrEmpty(message)) {
      if (message.contains("\n")) {
        Scanner scanner = new Scanner(message);
        scanner.useDelimiter("\n");
        Style s1 = firstStyle;
        Style s2 = secondStyle;
        while (scanner.hasNext()) {
          printMessageNaked(startWith, scanner.next(), s1, s2);
          // alternate between colors each newline
          Style cpy = s1;
          s1 = s2;
          s2 = cpy;
        }
      } else {
        TextComponent string =
            new StringTextComponent(startWith + message.replaceAll("\r", ""));
        string.setStyle(firstStyle);
        outputMessage(string.getFormattedText());
      }
    }
  }
  
  // private function that is ultimately used to output the message
  private static void outputMessage(String text) {
    // TODO:
    /*
    if (getLocalPlayer() != null) {
      getLocalPlayer().sendMessage(new StringTextComponent(text));
    } else if (MC.currentScreen instanceof CommandInputGui) {
      ((CommandInputGui) MC.currentScreen).print(text);
    }*/
  }

  @Deprecated
  public static void printMessageNaked(String append, String message, Style style) {
    printMessageNaked(append, message, style, style);
  }

  @Deprecated
  public static void printMessageNaked(String append, String message) {
    printMessageNaked(
        append,
        message,
        new Style().setColor(TextFormatting.WHITE),
        new Style().setColor(TextFormatting.GRAY));
  }
  @Deprecated
  public static void printMessageNaked(String message) {
    printMessageNaked("", message);
  }
  @Deprecated
  // Will append '[FH] ' in front
  public static void printMessage(String message) {
    if (!Strings.isNullOrEmpty(message)) {
      printMessageNaked("[FH] " + message);
    }
  }
  @Deprecated
  public static void printMessage(String format, Object... args) {
    printMessage(String.format(format, args));
  }
  
  private static ITextComponent getFormattedText(String text, TextFormatting color,
      boolean bold, boolean italic) {
    return new StringTextComponent(text.replaceAll("\r", ""))
        .setStyle(new Style()
            .setColor(color)
            .setBold(bold)
            .setItalic(italic)
        );
  }
  @Deprecated
  public static void printInform(String format, Object... args) {
    outputMessage(
        getFormattedText("[ForgeHax]", TextFormatting.GREEN, true, false)
            .appendSibling(
                getFormattedText(" " + String.format(format, args).trim(),
                    TextFormatting.GRAY, false, false)
            ).getFormattedText()
    );
  }
  @Deprecated
  public static void printWarning(String format, Object... args) {
    outputMessage(
        getFormattedText("[ForgeHax]", TextFormatting.YELLOW, true, false)
            .appendSibling(
                getFormattedText(" " + String.format(format, args).trim(),
                    TextFormatting.GRAY, false, false)
            ).getFormattedText()
    );
  }
  @Deprecated
  public static void printError(String format, Object... args) {
    outputMessage(
        getFormattedText("[ForgeHax]", TextFormatting.RED, true, false)
            .appendSibling(
                getFormattedText(" " + String.format(format, args).trim(),
                    TextFormatting.GRAY, false, false)
            ).getFormattedText()
    );
  }
  @Deprecated
  public static void printStackTrace(Throwable t) {
    getLog().error(Throwables.getStackTraceAsString(t));
  }
  @Deprecated
  public static void handleThrowable(Throwable t) {
    getLog().error(String.format("[%s] %s",
        t.getClass().getSimpleName(),
        Strings.nullToEmpty(t.getMessage())));
    
    if (t.getCause() != null) {
      handleThrowable(t.getCause());
    }
    printStackTrace(t);
  }
  @Deprecated
  public static void addScheduledTask(Runnable runnable) {
    // TODO: add this
  }
  @Deprecated
  public static void reloadChunks() {
    // credits to 0x22
    if (getWorld() != null && getLocalPlayer() != null) {
      addScheduledTask(
          () -> {
            BlockPos pos = getLocalPlayer().getPosition();
            
            int distance = MC.gameSettings.renderDistanceChunks * 16;
            
            MC.worldRenderer.markBlockRangeForRenderUpdate(
                pos.getX() - distance,
                pos.getY() - distance,
                pos.getZ() - distance,
                pos.getX() + distance,
                pos.getY() + distance,
                pos.getZ() + distance);
          });
    }
  }
  @Deprecated
  public static void reloadChunksHard() {
    addScheduledTask(() -> {
      if (getWorld() != null && getLocalPlayer() != null) {
        MC.worldRenderer.loadRenderers();
      }
    });
  }
}
