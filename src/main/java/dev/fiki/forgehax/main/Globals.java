package dev.fiki.forgehax.main;

import dev.fiki.forgehax.main.util.command.Command;
import dev.fiki.forgehax.main.util.command.CommandGlobal;
import dev.fiki.forgehax.main.util.mod.loader.ModManager;
import dev.fiki.forgehax.main.util.FileManager;
import net.minecraft.block.Block;
import net.minecraft.client.GameSettings;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.overlay.PlayerTabOverlayGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Optional;

/**
 * 2 lazy to import static
 */
public interface Globals {
  Logger LOGGER = LogManager.getLogger("ForgeHax");
  Minecraft MC = Minecraft.getInstance();
  Command GLOBAL_COMMAND = CommandGlobal.getInstance();

  //
  // forgehax
  //

  static Logger getLogger() {
    return LOGGER;
  }

  static ModManager getModManager() {
    return ModManager.getInstance();
  }

  static FileManager getFileManager() {
    return FileManager.getInstance();
  }

  //
  // forge
  //

  static ClassLoader getLauncherClassLoader() {
    // TODO: 1.15 make sure this is the correct classloader
    return Thread.currentThread().getContextClassLoader();
  }
  //
  // minecraft
  //

  static GameSettings getGameSettings() {
    return MC.gameSettings;
  }

  //
  // local player
  //

  static ClientPlayerEntity getLocalPlayer() {
    return MC.player;
  }

  static Entity getMountedEntity() {
    return getLocalPlayer() == null ? null : getLocalPlayer().getRidingEntity();
  }

  static Entity getMountedEntityOrPlayer() {
    return Optional.ofNullable(getMountedEntity()).orElse(getLocalPlayer());
  }

  static boolean isLocalPlayerRidingEntity() {
    return getMountedEntity() != null;
  }

  static PlayerController getPlayerController() {
    return MC.playerController;
  }

  //
  // client world
  //

  static ClientWorld getWorld() {
    return Objects.requireNonNull(getLocalPlayer()).worldClient;
  }

  static boolean isInWorld() {
    return getWorld() != null;
  }

  static void reloadChunks() {

  }

  //
  // game screen
  //

  static MainWindow getMainWindow() {
    return MC.getMainWindow();
  }

  static FontRenderer getFontRenderer() {
    return MC.fontRenderer;
  }

  static GameRenderer getGameRenderer() {
    return MC.gameRenderer;
  }

  static Screen getDisplayScreen() {
    return MC.currentScreen;
  }

  static void setDisplayScreen(Screen screen) {
    MC.displayGuiScreen(screen);
  }

  static void closeDisplayScreen() {
    setDisplayScreen(null);
  }

  static int getScreenWidth() {
    return getMainWindow().getScaledWidth();
  }

  static int getScreenHeight() {
    return getMainWindow().getScaledHeight();
  }

  //
  // registries
  //

  static IForgeRegistry<Block> getBlockRegistry() {
    return ForgeRegistries.BLOCKS;
  }

  //
  // networking
  //

  static NetworkManager getNetworkManager() {
    return MC.getConnection() != null ? MC.getConnection().getNetworkManager() : null;
  }

  static boolean sendNetworkPacket(IPacket<?> packet) {
    NetworkManager nm = getNetworkManager();
    if(nm != null) {
      nm.sendPacket(packet);
      return true;
    }
    return false;
  }

  //
  // scheduler
  //

  static void addScheduledTask(Runnable runnable) {
    // TODO: scheduler

  }

  //
  // text output
  //

  static void print(String str, Object... fmt) {
    // TODO:
  }

  static void printInform(String str, Object... fmt) {
    // TODO:
  }

  static void printWarning(String str, Object... fmt) {
    // TODO:
  }

  static void printError(String str, Object... fmt) {
    // TODO:
  }
}
