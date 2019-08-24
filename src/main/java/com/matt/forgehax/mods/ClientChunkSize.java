package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getFileManager;
import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getWorld;

import com.matt.forgehax.asm.reflection.FastReflection.Methods;
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
  
  private static final File DUMMY = getFileManager().getBaseResolve("dummy").toFile();
  
  private final SimpleTimer timer = new SimpleTimer();
  
  private boolean running = false;
  private long size = 0L;
  private long previousSize = 0L;
  private ChunkPos current = null;
  
  public ClientChunkSize() {
    super(Category.MISC, "ClientChunkSize", false, "Shows the client-side chunk size in bytes");
  }
  
  private static String toFormattedBytes(long size) {
    NumberFormat format = NumberFormat.getInstance();
    format.setGroupingUsed(true);
    if (size < 1000) // less than 1KB
    {
      return format.format(size) + " B";
    } else if (size < 1000000) // less than 1MB
    {
      return format.format((double) size / 1000.D) + " KB";
    } else {
      return format.format((double) size / 1000000.D) + " MB";
    }
  }
  
  private static String difference(long size) {
    if (size == 0) {
      return "+0 B";
    }
    if (size > 0) {
      return "+" + toFormattedBytes(size);
    } else {
      return "-" + toFormattedBytes(Math.abs(size));
    }
  }
  
  @Override
  protected void onEnabled() {
    timer.reset();
    running = false;
    size = previousSize = 0L;
    current = null;
  }
  
  @Override
  public String getDisplayText() {
    return super.getDisplayText()
      + " "
      + String.format(
      "[%s | %s]",
      size == -1 ? "<error>" : toFormattedBytes(size), difference(size - previousSize));
  }
  
  @SubscribeEvent
  public void onTick(ClientTickEvent event) {
    if (getWorld() == null || getLocalPlayer() == null || running) {
      return;
    }
    
    switch (event.phase) {
      case END: {
        Chunk chunk = getWorld().getChunkFromBlockCoords(getLocalPlayer().getPosition());
        if (chunk.isEmpty()) {
          return;
        }
    
        ChunkPos pos = chunk.getPos();
        if (!pos.equals(current) || (timer.isStarted() && timer.hasTimeElapsed(1000L))) {
          // chunk changed, don't show diff between different chunks
          if (current != null && !pos.equals(current)) {
            size = previousSize = 0L;
          }
      
          current = pos;
          running = true;
      
          // process size calculation on another thread
          Executors.defaultThreadFactory()
            .newThread(
              () -> {
                try {
                  final NBTTagCompound root = new NBTTagCompound();
                  NBTTagCompound level = new NBTTagCompound();
                  root.setTag("Level", level);
                  root.setInteger("DataVersion", 1337);
              
                  try {
                    // this should be done on the main mc thread but it works 99% of the
                    // time outside it
                    AnvilChunkLoader loader = new AnvilChunkLoader(DUMMY, null);
                    Methods.AnvilChunkLoader_writeChunkToNBT.invoke(
                      loader, chunk, getWorld(), level);
                  } catch (Throwable t) {
                    size = -1L;
                    previousSize = 0L;
                    return; // couldn't save chunk
                  }
              
                  DataOutputStream compressed =
                    new DataOutputStream(
                      new BufferedOutputStream(
                        new DeflaterOutputStream(new ByteArrayOutputStream(8096))));
                  try {
                    CompressedStreamTools.write(root, compressed);
                    previousSize = size;
                    size = compressed.size();
                  } catch (IOException e) {
                    size = -1L;
                    previousSize = 0L;
                  }
                } finally {
                  timer.start();
                  running = false;
                }
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
