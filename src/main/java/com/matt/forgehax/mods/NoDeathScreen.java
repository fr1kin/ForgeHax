package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;

import com.matt.forgehax.util.command.Setting;//TODO implement some settings for fancy shit
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.matt.forgehax.asm.events.PacketEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.client.CPacketKeepAlive;
import net.minecraft.util.text.TextFormatting;

@RegisterMod
public class NoDeathScreen extends ToggleMod {

  private final Setting<Boolean> silent =
  getCommandStub()
      .builders()
      .<Boolean>newSettingBuilder()
      .name("silent")
      .description("Cancel all your packets while dead")
      .defaultTo(true)
      .build();

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
        .description("send the respawn packet")
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
      return (getModName() + " [" + TextFormatting.RED + "DEAD" + TextFormatting.WHITE + "]");
    return (getModName());
  }

  @SubscribeEvent
  public void onOutgoingPacketSent(PacketEvent.Outgoing.Pre event) {
    if (MC.player == null) {
      dead = false;
      return; // Don't mess with menus! 
    }
    if (silent.get() && dead && !(event.getPacket() instanceof CPacketChatMessage)
         && !(event.getPacket() instanceof CPacketKeepAlive)) {
      event.setCanceled(true);
    }
  }

  @Override
  public void onDisabled() {
    dead = false;
    if (MC.player == null) return;
    MC.player.respawnPlayer();
    MC.player.capabilities.allowFlying = false;
  }

  @SubscribeEvent
  public void onGuiScreen(GuiScreenEvent event) {
    if (event.getGui() instanceof GuiGameOver) {
      dead = true;
      MC.displayGuiScreen(new GuiChat("Oh geez guess I'm bad at this game"));
      MC.player.respawnPlayer();
      getLocalPlayer().setHealth(1F);
      getLocalPlayer().isDead = false;
      MC.player.capabilities.allowFlying = true;
    }
  }
} 
