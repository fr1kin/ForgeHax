package com.matt.forgehax.mods.infodisplay;

import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.matt.forgehax.asm.events.PacketEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.network.play.server.SPacketTimeUpdate;

@RegisterMod
public class WorldTime extends ToggleMod {

  public final Setting<Boolean> serverSync =
    getCommandStub()
      .builders()
      .<Boolean>newSettingBuilder()
      .name("server-sync")
      .description("Force synchronization with server time.")
      .defaultTo(false)
      .build();

  public final Setting<Boolean> slim =
    getCommandStub()
      .builders()
      .<Boolean>newSettingBuilder()
      .name("slim")
      .description("Show the time with a slimmer, real-like format.")
      .defaultTo(false)
      .build();

  public WorldTime() {
    super(Category.GUI, "WorldTime", false, "Shows the time in Minecraft world.");
  }

  @Override
  public boolean isInfoDisplayElement() {
    return true;
  }

  private final int TPS = 20;
  private int time = 0;
  private long day = 0;

  @SubscribeEvent
  public void onPacketPreceived(PacketEvent.Incoming.Pre event) {
    if (serverSync.get() && event.getPacket() instanceof SPacketTimeUpdate) {
      time = (int) (((SPacketTimeUpdate) event.getPacket()).getWorldTime() % 24000L);
      day = ((SPacketTimeUpdate) event.getPacket()).getWorldTime() / 24000L;
    }
  }

  public String getInfoDisplayText() {
    if (!serverSync.get()) {
      time = (int) (MC.world.getWorldTime() % 24000L);
      day = MC.world.getWorldTime() / 24000L;
    }

    if (slim.get()) {
      return String.format("Day %d, %s", day, translate(time));
    } else {
      return String.format("Age: %d | Time: %d/%d [%s]", day, time / TPS, getNextStep(time) / TPS, getTimePhase(time));
    }
  }

  private String getTimePhase(int time) {
    if (time > 23000) return "Dawn";
    if (time > 18500) return "Night";
    if (time > 17500) return "Midnight";
    if (time > 13000) return "Evening";
    if (time > 12000) return "Dusk";
    if (time > 6500) return "Afternoon";
    if (time > 5500) return "Noon";
    return "Morning";
  }

  private int getNextStep(int time) {
    if (time > 23000) return 24000;
    if (time > 13000) return 23000;
    if (time > 12000) return 13000;
    return 12000;
  }

  private String translate(int time) {
    int translated_time = (int) (((long) time * 1728000L) / 24000L); // "ticks" in a real day / ticks in minecraft
    return String.format("%02d:%02d", translated_time / (3600 * TPS), translated_time % (3600 * TPS) / (60 * TPS));
  }
}
