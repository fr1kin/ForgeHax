package com.matt.forgehax.mods.commands;

import com.matt.forgehax.util.command.Command;
import com.matt.forgehax.util.command.CommandBuilders;
import com.matt.forgehax.util.gui.mc.MinecraftGuiProxy;
import com.matt.forgehax.util.gui.test.GuiTestMain;
import com.matt.forgehax.util.mod.CommandMod;
import org.lwjgl.glfw.GLFW;

/** Created on 9/12/2017 by fr1kin */
// @RegisterMod
public class GuiCommand extends CommandMod {
  private final MinecraftGuiProxy gui = new MinecraftGuiProxy(new GuiTestMain());

  private MinecraftGuiProxy getGui() {
    return new MinecraftGuiProxy(new GuiTestMain()); // gui;
  }

  public GuiCommand() {
    super("GuiCommand");
  }

  @RegisterCommand
  public Command gui(CommandBuilders builders) {
    return builders
        .newStubBuilder()
        .name("gui")
        .description("Forgehax gui")
        .bind(GLFW.GLFW_KEY_INSERT)
        .kpressed(cb -> MC.displayGuiScreen(getGui()))
        .build();
  }
}
