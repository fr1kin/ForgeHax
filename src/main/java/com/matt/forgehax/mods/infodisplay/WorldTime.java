package com.matt.forgehax.mods.infodisplay;

import static com.matt.forgehax.Helper.getWorld;

import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.matt.forgehax.asm.events.PacketEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.network.play.server.SPacketTimeUpdate;

@RegisterMod
public class WorldTime extends ToggleMod {

  public final Setting<Boolean> server_sync =
    getCommandStub()
      .builders()
      .<Boolean>newSettingBuilder()
      .name("server-sync")
      .description("Force synchronization with server time")
      .defaultTo(false)
      .build();


  public WorldTime() {
    super(Category.GUI, "WorldTime", false, "Shows the time in Minecraft world");
  }

  @Override
  public boolean isInfoDisplayElement() {
    return true;
  }

  private int TPS = 20;
  private int time = 0;

  @SubscribeEvent
  public void onPacketPreceived(PacketEvent.Incoming.Pre event) {
    if (server_sync.get() && event.getPacket() instanceof SPacketTimeUpdate) {
      time = (int) (((SPacketTimeUpdate) event.getPacket()).getTotalWorldTime() % 24000L);
    }
  }

  public String getInfoDisplayText() {
    if (!server_sync.get()) {
      time = (int) (getWorld().getWorldTime() % 24000L);
    }
    return String.format("Time: %d/%d [%s]", time/TPS, getNextStep(time)/TPS, getTimePhase(time));
  }

  private String getTimePhase(int time) {
    if (time > 23000) return "Dawn";
    if (time > 18500) return "Night";
    if (time > 17500) return "Midnight";
    if (time > 13000) return "Night";
    if (time > 12000) return "Dusk";
    if (time > 6500) return "Day";
    if (time > 5500) return "Noon";
    return "Day";
  }

  private int getNextStep(int time) {
    if (time > 23000) return 24000;
    if (time > 13000) return 23000;
    if (time > 12000) return 13000;
    return 12000;
  }
}
