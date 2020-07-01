package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getNetworkManager;

import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.gui.GuiSleepMP;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class BedModeMod extends ToggleMod {
  
  public BedModeMod() {
    super(Category.MISC, "BedMode", false, "Sleep walking");
  }

  private boolean sleeping = false;

  @Override
  public String getDisplayText() {
    if (sleeping)
      return (getModName() + " [" + TextFormatting.BLUE + "SLEEPING" + TextFormatting.WHITE + "]");
    return (getModName());
  }

  @Override
  public void onLoad() {
    getCommandStub()
        .builders()
        .newCommandBuilder()
        .name("exit-bed")
        .description("Send the exit-bed packet")
        .processor(
            data -> {
              sleeping = false;
              getNetworkManager()
                .sendPacket(new CPacketEntityAction(getLocalPlayer(), Action.STOP_SLEEPING));
            })
        .build();
  }

  @Override
  public void onDisabled() {
    if (getLocalPlayer() != null && sleeping) {
      sleeping = false;
      getNetworkManager()
        .sendPacket(new CPacketEntityAction(getLocalPlayer(), Action.STOP_SLEEPING));
    }
  }

  @SubscribeEvent
  public void onPacketInbound(PacketEvent.Incoming.Pre event) {
    if (getLocalPlayer() == null) return;
    if (event.getPacket() instanceof SPacketPlayerPosLook) {
      sleeping = false;
    } 
  }
  
  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    FastReflection.Fields.EntityPlayer_sleeping.set(getLocalPlayer(), false);
    FastReflection.Fields.EntityPlayer_sleepTimer.set(getLocalPlayer(), 0);
  }
  
  @SubscribeEvent
  public void onGuiUpdate(GuiOpenEvent event) {
    if (event.getGui() instanceof GuiSleepMP) {
      event.setCanceled(true);
      sleeping = true;
    }
  }
}
