package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getFileManager;
import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getWorld;

import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.util.SimpleTimer;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.concurrent.Executors;
import java.util.zip.DeflaterOutputStream;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

@RegisterMod
public class ClientChunkSize extends ToggleMod {
  private static final File DUMMY = new File(getFileManager().getBaseDirectory(), "dummy");

  private final SimpleTimer timer = new SimpleTimer();

  private boolean running = false;
  private long size = 0L;
  private long compressedSize = 0L;
  private ChunkPos current = null;

  public ClientChunkSize() {
    super(Category.MISC, "ClientChunkSize", false, "Shows the client-side chunk size in bytes");
  }

  private static String asString(long size) {
    if (size < 0) return "<error>";

    NumberFormat format = NumberFormat.getInstance();
    format.setGroupingUsed(true);
    return format.format((double) size / 1000.D) + " KB";
  }

  @Override
  protected void onEnabled() {
    timer.reset();
    running = false;
    size = compressedSize = 0L;
    current = null;
  }

  @Override
  public String getDisplayText() {
    return super.getDisplayText()
        + " "
        + String.format("[%s | compressed %s]", asString(size), asString(compressedSize));
  }

  @SubscribeEvent
  public void onTick(ClientTickEvent event) {
    if (getWorld() == null || getLocalPlayer() == null || running) return;

    switch (event.phase) {
      case END:
        {
          Chunk chunk = getWorld().getChunkFromBlockCoords(getLocalPlayer().getPosition());
          if (chunk.isEmpty()) return;

          ChunkPos pos = chunk.getPos();
          if (!pos.equals(current) || (timer.isStarted() && timer.hasTimeElapsed(1000L))) {
            current = pos;

            // this probably needs to be done on the main thread since it loops over entities
            final NBTTagCompound root = new NBTTagCompound();
            NBTTagCompound level = new NBTTagCompound();
            root.setTag("Level", level);
            root.setInteger("DataVersion", 7777);

            try {
              AnvilChunkLoader loader = new AnvilChunkLoader(DUMMY, null);
              FastReflection.Methods.AnvilChunkLoader_writeChunkToNBT.invoke(
                  loader, chunk, getWorld(), level);
            } catch (Throwable t) {
              size = compressedSize = -1L;
              return; // couldn't save chunk
            }

            running = true;

            // process size calculation on another thread
            Executors.defaultThreadFactory()
                .newThread(
                    () -> {
                      DataOutputStream uncompressed =
                          new DataOutputStream(
                              new BufferedOutputStream(new ByteArrayOutputStream(8096)));
                      DataOutputStream compressed =
                          new DataOutputStream(
                              new BufferedOutputStream(
                                  new DeflaterOutputStream(new ByteArrayOutputStream(8096))));
                      try {
                        CompressedStreamTools.write(root, uncompressed);
                        CompressedStreamTools.write(root, compressed);
                        size = uncompressed.size();
                        compressedSize = compressed.size();
                      } catch (IOException e) {
                        size = -1L;
                        compressedSize = -1L;
                      }

                      timer.start();
                    })
                .start();
          }
          break;
        }
      default:
        break;
    }
  }
}
