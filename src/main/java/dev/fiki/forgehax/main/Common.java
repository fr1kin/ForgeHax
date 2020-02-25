package dev.fiki.forgehax.main;

import dev.fiki.forgehax.main.util.FileManager;
import dev.fiki.forgehax.main.util.TextComponentBuilder;
import dev.fiki.forgehax.main.util.cmd.RootCommand;
import dev.fiki.forgehax.main.util.cmd.execution.IConsole;
import dev.fiki.forgehax.main.util.draw.BufferProvider;
import dev.fiki.forgehax.main.util.mod.loader.ModManager;
import net.minecraft.block.Block;
import net.minecraft.client.GameSettings;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface Common {
  Minecraft MC = Minecraft.getInstance();

  //
  // forgehax
  //

  static Logger getLogger() {
    return ForgeHax.getInstance().getLogger();
  }

  static RootCommand getRootCommand() {
    return ForgeHax.getInstance().getRootCommand();
  }

  static ModManager getModManager() {
    return ForgeHax.getInstance().getModManager();
  }

  static FileManager getFileManager() {
    return ForgeHax.getInstance().getFileManager();
  }

  static ForgeHax.ConfigProperties getConfigProperties() {
    return ForgeHax.getInstance().getConfigProperties();
  }

  static Path getBaseDirectory() {
    return ForgeHax.getInstance().getBaseDirectory();
  }

  static IConsole getCurrentConsoleOutput() {
    return ForgeHax.getInstance().getCurrentConsole();
  }

  static BufferProvider getBufferProvider() {
    return ForgeHax.getInstance().getBufferProvider();
  }

  //
  // forge
  //

  static ClassLoader getLauncherClassLoader() {
    return FMLLoader.getLaunchClassLoader();
  }

  //
  // minecraft
  //

  static GameSettings getGameSettings() {
    return MC.gameSettings;
  }

  static Stream<Entity> worldEntities() {
    return !isInWorld() ? Stream.empty()
        : StreamSupport.stream(getWorld().getAllEntities().spliterator(), false);
  }

  static Stream<TileEntity> worldTileEntities() {
    return !isInWorld() ? Stream.empty()
        : getWorld().loadedTileEntityList.stream();
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

  @Nonnull // just to get the IDE to shutup
  static ClientWorld getWorld() {
    return getLocalPlayer() == null ? null : getLocalPlayer().worldClient;
  }

  static boolean isInWorld() {
    return getWorld() != null;
  }

  static void reloadChunkSmooth() {
    addScheduledTask(() -> {
      if(isInWorld()) {
        int x = (int) getLocalPlayer().getPosX();
        int y = (int) getLocalPlayer().getPosY();
        int z = (int) getLocalPlayer().getPosZ();

        int distance = getGameSettings().renderDistanceChunks * 16;

        getWorldRenderer().markBlockRangeForRenderUpdate(
            x - distance, y - distance,
            z - distance, x + distance,
            y + distance, z + distance
        );
      }
    });
  }

  static void reloadChunks() {
    addScheduledTask(() -> {
      if(isInWorld()) {
        getWorldRenderer().loadRenderers();
      }
    });
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

  static WorldRenderer getWorldRenderer() {
    return MC.worldRenderer;
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
    if (nm != null) {
      nm.sendPacket(packet);
      return true;
    }
    return false;
  }

  //
  // scheduler
  //

  static void requiresMainThreadExecution() {
    if(!MC.isOnExecutionThread()) {
      throw new IllegalStateException("Must be executed on main thread!");
    }
  }

  static Executor getMainThreadExecutor() {
    return MC;
  }

  static Executor getAsyncThreadExecutor() {
    return ForgeHax.getInstance().getAsyncExecutorService();
  }

  static ExecutorService getPooledThreadExecutor() {
    return ForgeHax.getInstance().getPooledExecutorService();
  }

  static void addScheduledTask(Runnable runnable) {
    getMainThreadExecutor().execute(runnable);
  }

  static void addAsyncScheduledTask(Runnable runnable) {
    getAsyncThreadExecutor().execute(runnable);
  }

  //
  // text output
  //

  static void printMessage(ITextComponent component) {
    if(getLocalPlayer() != null) {
      getLocalPlayer().sendStatusMessage(component, false);
    }
  }

  static void printColored(TextFormatting formatting, String text) {
    if (getLocalPlayer() != null) {
      printMessage(TextComponentBuilder.builder()
          .color(formatting)
          .text("> ")
          .color(TextFormatting.WHITE)
          .text(text)
          .build());
      getLogger().info("ForgeHax message: {}", text);
    }
  }

  static void print(String str, Object... fmt) {
    printColored(TextFormatting.GRAY, String.format(str, fmt));
  }

  static void printInform(String str, Object... fmt) {
    printColored(TextFormatting.GREEN, String.format(str, fmt));
  }

  static void printWarning(String str, Object... fmt) {
    printColored(TextFormatting.YELLOW, String.format(str, fmt));
  }

  static void printError(String str, Object... fmt) {
    printColored(TextFormatting.RED, String.format(str, fmt));
  }
}
