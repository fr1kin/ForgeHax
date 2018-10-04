package com.matt.forgehax;

import com.matt.forgehax.util.command.Command;
import com.matt.forgehax.util.command.CommandGlobal;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** 2 lazy to import static */
public interface Globals {
  Logger LOGGER = LogManager.getLogger("ForgeHax");
  Minecraft MC = FMLClientHandler.instance().getClient();
  Command GLOBAL_COMMAND = CommandGlobal.getInstance();
}
