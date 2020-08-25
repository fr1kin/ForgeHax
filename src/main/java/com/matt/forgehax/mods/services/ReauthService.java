package com.matt.forgehax.mods.services;

import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.mods.managers.AccountManager;
import com.matt.forgehax.util.SimpleTimer;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.matt.forgehax.Helper.getLog;

/**
 * Added by OverFloyd & TotalDarkness
 * July 2020
 */
@RegisterMod
public class ReauthService extends ToggleMod {

  private final Setting<String> discordWebhookUrl =
    getCommandStub()
      .builders()
      .<String>newSettingBuilder()
      .name("webhook-url")
      .description("The discord web-hook link to send notifications.")
      .defaultTo("")
      .build();

  private final Setting<Boolean> sendDiscordMsgs =
    getCommandStub()
      .builders()
      .<Boolean>newSettingBuilder()
      .name("send-discord-msgs")
      .description("Send discord notifications.")
      .defaultTo(false)
      .build();

  public final Setting<Integer> delay =
    getCommandStub()
      .builders()
      .<Integer>newSettingBuilder()
      .name("delay")
      .description("Delay in ms between each auth attempt.")
      .defaultTo(240000) // 4 minutes
      .build();

  public ReauthService() {
    super(Category.MISC, "ReauthService", false, "Reauths on invalid session.");
  }

  private final SimpleTimer timer = new SimpleTimer();
  boolean isSessionValid;
  int counter;

  @SubscribeEvent
  public void guiOpen(final GuiOpenEvent event) {
    if (event.getGui() instanceof GuiDisconnected) {
      final String disconnectMsg = getDisconnectedMsg((GuiDisconnected) event.getGui());

      if (disconnectMsg.contains("Failed to login")) {

        // Anti auth-spam if there's too many failed attempts
        if (!isSessionValid && !timer.hasTimeElapsed(delay.get())) {
          int countdown = (int) (delay.get() - timer.getTimeElapsed());

          getLog().info("Too many failed attempts, wait " + countdown + " ms.");
          return;
        }

        final String alias = FastReflection.Fields.Minecraft_session.get(MC).getUsername();

        if (AccountManager.INSTANCE.login(alias)) {
          isSessionValid = true;
        } else {
          isSessionValid = false;
          counter++;
        }

        // Start timer to prevent being auth-blocked by moejang
        if (counter > 2) {
          timer.start();
          counter = 0; // Reset counter, else it keeps going after the delay has elapsed
        }
      }
    }
  }

  private String getDisconnectedMsg(GuiDisconnected disconnected) {
    String message;

    try {
      message = FastReflection.Fields.GuiDisconnected_message.get(disconnected).getUnformattedText();
    } catch (Exception e) {
      message = e.getMessage(); // do this cause lazy
    }

    return message;
  }
}
