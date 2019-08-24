package com.matt.forgehax.mods;

import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.matt.forgehax.Helper.getFileManager;
import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.printInform;
import static java.util.Objects.nonNull;

@RegisterMod
public class CoordsFinder extends ToggleMod {
  @SuppressWarnings("WeakerAccess")
  public final Setting<Boolean> lightning =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("lighting")
          .description("log lightning strikes")
          .defaultTo(true)
          .build();

  @SuppressWarnings("WeakerAccess")
  public final Setting<Integer> minLightningDist =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("lighting-min-dist")
          .description("how far a lightning strike has to be from you to get logged")
          .min(0)
          .defaultTo(32)
          .build();

  private final Path log = getFileManager().getBaseResolve("logs/coordsfinder.log");
  private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");
  private BufferedWriter logWriter;

  public CoordsFinder() {
    super(Category.MISC, "CoordsFinder", false, "Logs coordinates of lightning strikes and teleports");
  }

  @Override
  protected void onEnabled() {
    super.onEnabled();

    try {
      if (!Files.exists(log)) Files.createFile(log);

      logWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(log.toFile(), true)));
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  @Override
  protected void onDisabled() {
    try {
      if (nonNull(logWriter))
        logWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      logWriter = null;
    }

    super.onDisabled();
  }

  private void logCoords(String name, double x, double y, double z) {
    int ix = MathHelper.floor(x);
    int iy = MathHelper.floor(y);
    int iz = MathHelper.floor(z);

    printInform("%s: %s @ [x:%d, y:%d, z:%d]", getModName(), name, ix, iy, iz);

    if (nonNull(logWriter)) {
      try {
        logWriter.write(String.format("[%s][%s][%d,%d,%d]", timeFormat.format(new Date()), name, ix, iy, iz));
        logWriter.newLine();
        logWriter.flush();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void logCoordsOnMinecraftThread(String name, double x, double y, double z) {
    MC.addScheduledTask(() -> logCoords(name, x, y, z));
  }

  @SubscribeEvent
  public void onPacketRecieving(PacketEvent.Incoming.Pre event) {
    if (lightning.get() && event.getPacket() instanceof SPacketSoundEffect) {
      SPacketSoundEffect packet = event.getPacket();

      // in the SPacketSpawnGlobalEntity constructor, this is only set to 1 if it's a lightning bolt
      if (packet.getSound() != SoundEvents.ENTITY_LIGHTNING_THUNDER) return;

      BlockPos pos = new BlockPos(packet.getX(), packet.getY(), packet.getZ());
      EntityPlayer player = getLocalPlayer();

      if (player.getDistanceSqToCenter(pos) >= Math.pow(minLightningDist.get(), 2))
        logCoordsOnMinecraftThread("Lightning", pos.getX(), pos.getY(), pos.getZ());
    }
  }
}
