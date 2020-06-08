package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;

import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.Helper;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.client.CPacketKeepAlive;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

@RegisterMod
public class NoDeathScreen extends ToggleMod {

  private boolean dead = false;
  
  public NoDeathScreen() {
    super(Category.COMBAT, "NoDeathScreen", false, "Allows you to fly around after you died");
  }

  @Override
  public void onLoad() {
    getCommandStub()
        .builders()
        .newCommandBuilder()
        .name("respawn")
        .description("self explainatory ig")
        .processor(
            data -> {
              dead = false;
              MC.player.respawnPlayer();
              MC.player.capabilities.allowFlying = false;
            })
        .build();
  }


  @Override
  public String getDisplayText() {
    if (dead)
      return (getModName() + " [DEAD]");
    return (getModName());
  }

  @SubscribeEvent
  public void onOutgoingPacketSent(PacketEvent.Outgoing.Pre event) {
    if (dead && !(event.getPacket() instanceof CPacketChatMessage)
         && !(event.getPacket() instanceof CPacketKeepAlive)) {
      event.setCanceled(true);
    }
  }

  @Override
  public void onDisabled() {
    dead = false;
    MC.player.respawnPlayer();
    MC.player.capabilities.allowFlying = false;
  }

  @SubscribeEvent
  public void onGuiScreen(GuiScreenEvent event) {
    if (event.getGui() instanceof GuiGameOver) {
      dead = true;
      getLocalPlayer().setHealth(1F);
      MC.player.capabilities.allowFlying = true;
      MC.displayGuiScreen(new GuiChat("Oh geez guess I'm bad at this game"));
    }
  }
} 
