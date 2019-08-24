package com.matt.forgehax.mods;

import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketEntityTeleport;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.matt.forgehax.Helper.getFileManager;
import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getWorld;
import static com.matt.forgehax.Helper.printInform;
import static java.util.Objects.isNull;

@RegisterMod
public class CoordsFinder extends ToggleMod {
  @SuppressWarnings("WeakerAccess")
  public final Setting<Boolean> logLightning =
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

  @SuppressWarnings("WeakerAccess")
  public final Setting<Boolean> logWolf =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("wolf")
          .description("log wolf teleports")
          .defaultTo(true)
          .build();

  @SuppressWarnings("WeakerAccess")
  public final Setting<Integer> minWolfDist =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("wolf-min-dist")
          .description("how far a wolf teleport has to be from you to get logged")
          .min(0)
          .defaultTo(256)
          .build();

  @SuppressWarnings("WeakerAccess")
  public final Setting<Boolean> logPlayer =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("player")
          .description("log player teleports")
          .defaultTo(true)
          .build();

  @SuppressWarnings("WeakerAccess")
  public final Setting<Integer> minPlayerDist =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("player-min-dist")
          .description("how far a player teleport has to be from you to get logged")
          .min(0)
          .defaultTo(256)
          .build();

  private final Path logPath = getFileManager().getBaseResolve("logs/coordsfinder.log");
  private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");

  public CoordsFinder() {
    super(Category.MISC, "CoordsFinder", false, "Logs coordinates of lightning strikes and teleports");
  }

  private void logCoords(String name, double x, double y, double z) {
    int ix = MathHelper.floor(x);
    int iy = MathHelper.floor(y);
    int iz = MathHelper.floor(z);

    printInform("%s > [x:%d, y:%d, z:%d]", name, ix, iy, iz);

    try {
      String toWrite = String.format("[%s][%s][%d,%d,%d]\n", timeFormat.format(new Date()), name, ix, iy, iz);
      Files.write(logPath, toWrite.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void logCoordsOnMinecraftThread(String name, double x, double y, double z) {
    MC.addScheduledTask(() -> logCoords(name, x, y, z));
  }

  private boolean pastDistance(EntityPlayer player, BlockPos pos, double dist) {
    return player.getDistanceSqToCenter(pos) >= Math.pow(dist, 2);
  }

  @SubscribeEvent
  public void onPacketRecieving(PacketEvent.Incoming.Pre event) {
    EntityPlayer player = getLocalPlayer();
    WorldClient world = getWorld();
    if (isNull(player) || isNull(world)) return;

    if (logLightning.get() && event.getPacket() instanceof SPacketSoundEffect) {
      SPacketSoundEffect packet = event.getPacket();

      // in the SPacketSpawnGlobalEntity constructor, this is only set to 1 if it's a lightning bolt
      if (packet.getSound() != SoundEvents.ENTITY_LIGHTNING_THUNDER) return;

      BlockPos pos = new BlockPos(packet.getX(), packet.getY(), packet.getZ());

      if (pastDistance(player, pos, minLightningDist.get()))
        logCoordsOnMinecraftThread("Lightning strike", pos.getX(), pos.getY(), pos.getZ());
    } else if (event.getPacket() instanceof SPacketEntityTeleport) {
      SPacketEntityTeleport packet = event.getPacket();
      Entity teleporting = world.getEntityByID(packet.getEntityId());
      BlockPos pos = new BlockPos(packet.getX(), packet.getY(), packet.getZ());

      if (logWolf.get() && teleporting instanceof EntityWolf) {
        if (pastDistance(player, pos, minWolfDist.get()))
          logCoordsOnMinecraftThread("Wolf teleport", packet.getX(), packet.getY(), packet.getZ());
      } else if (logPlayer.get() && teleporting instanceof EntityPlayer) {
        if (pastDistance(player, pos, minPlayerDist.get()))
          logCoordsOnMinecraftThread(String.format("Player teleport (%s)", teleporting.getName()), packet.getX(), packet.getY(), packet.getZ());
      }
    }
  }
}
