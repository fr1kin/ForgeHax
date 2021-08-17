package dev.fiki.forgehax.main;

import dev.fiki.forgehax.api.FileManager;
import dev.fiki.forgehax.api.TextComponentBuilder;
import dev.fiki.forgehax.api.cmd.RootCommand;
import dev.fiki.forgehax.api.draw.BufferProvider;
import dev.fiki.forgehax.api.event.EventBus;
import dev.fiki.forgehax.api.modloader.ModManager;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL;

public interface Common {
  Minecraft MC = Minecraft.getInstance();

  //
  // forgehax
  //

  static ForgeHax getForgeHax() {
    return ForgeHax.getInstance();
  }

  static Logger getLogger() {
    return LogManager.getLogger();
  }

  static RootCommand getRootCommand() {
    return getForgeHax().getRootCommand();
  }

  static ModManager getModManager() {
    return getForgeHax().getModManager();
  }

  static FileManager getFileManager() {
    return getForgeHax().getFileManager();
  }

  static ForgeHax.ConfigProperties getConfigProperties() {
    return getForgeHax().getConfigProperties();
  }

  static Path getBaseDirectory() {
    return getForgeHax().getBaseDirectory();
  }

  static BufferProvider getBufferProvider() {
    return getForgeHax().getBufferProvider();
  }

  static EventBus getEventBus() {
    return getForgeHax().getEventBus();
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
    return MC.options;
  }

  static Stream<Entity> worldEntities() {
    return !isInWorld() ? Stream.empty()
        : StreamSupport.stream(getWorld().entitiesForRendering().spliterator(), false);
  }

  static Stream<TileEntity> worldTileEntities() {
    return !isInWorld() ? Stream.empty()
        : getWorld().tickableBlockEntities.stream();
  }

  //
  // local player
  //

  static ClientPlayerEntity getLocalPlayer() {
    return MC.player;
  }

  static Entity getMountedEntity() {
    return getLocalPlayer() == null ? null : getLocalPlayer().getVehicle();
  }

  static Entity getMountedEntityOrPlayer() {
    return Optional.ofNullable(getMountedEntity()).orElse(getLocalPlayer());
  }

  static boolean isLocalPlayerRidingEntity() {
    return getMountedEntity() != null;
  }

  static PlayerController getPlayerController() {
    return MC.gameMode;
  }

  //
  // client world
  //

  @Nonnull // just to get the IDE to shutup
  static ClientWorld getWorld() {
    return getLocalPlayer() == null ? null : getLocalPlayer().clientLevel;
  }

  static boolean isInWorld() {
    return getWorld() != null;
  }

  static void reloadChunkSmooth() {
    addScheduledTask(() -> {
      if (isInWorld()) {
        int x = (int) getLocalPlayer().getX() >> 4;
        int z = (int) getLocalPlayer().getZ() >> 4;

        int distance = getGameSettings().renderDistance;

        for (int i = x - distance; i < x + distance; i++) {
          for (int k = z - distance; k < z + distance; k++) {
            for (int j = 0; j < 16; j++) {
              getWorldRenderer().setSectionDirty(i, j, k);
            }
          }
        }
      }
    });
  }

  static void reloadChunks() {
    addScheduledTask(() -> {
      if (isInWorld()) {
        getWorldRenderer().allChanged();
      }
    });
  }

  //
  // game screen
  //

  static MainWindow getMainWindow() {
    return MC.getWindow();
  }

  static FontRenderer getFontRenderer() {
    return MC.font;
  }

  static GameRenderer getGameRenderer() {
    return MC.gameRenderer;
  }

  static WorldRenderer getWorldRenderer() {
    return MC.levelRenderer;
  }

  static Screen getDisplayScreen() {
    return MC.screen;
  }

  static void setDisplayScreen(Screen screen) {
    MC.setScreen(screen);
  }

  static void closeDisplayScreen() {
    setDisplayScreen(null);
  }

  static int getScreenWidth() {
    return getMainWindow().getGuiScaledWidth();
  }

  static int getScreenHeight() {
    return getMainWindow().getGuiScaledHeight();
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
    return MC.getConnection() != null ? MC.getConnection().getConnection() : null;
  }

  static boolean sendNetworkPacket(IPacket<?> packet) {
    NetworkManager nm = getNetworkManager();
    if (nm != null) {
      nm.send(packet);
      return true;
    }
    return false;
  }

  //
  // scheduler
  //

  static void requiresMainThreadExecution() {
    if (!MC.isSameThread()) {
      throw new IllegalStateException("Must be executed on main thread!");
    }
  }

  static Executor getMainThreadExecutor() {
    return MC;
  }

  static Executor getAsyncThreadExecutor() {
    return getForgeHax().getAsyncExecutorService();
  }

  static ExecutorService getPooledThreadExecutor() {
    return getForgeHax().getPooledExecutorService();
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
    if (getLocalPlayer() != null) {
      getLocalPlayer().displayClientMessage(component, false);
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

  static boolean debuggerReleaseControl() {
    GLFW.glfwSetInputMode(Minecraft.getInstance().getWindow().getWindow(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
    return true;
  }
}
