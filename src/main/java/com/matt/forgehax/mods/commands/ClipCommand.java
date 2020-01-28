package com.matt.forgehax.mods.commands;

import com.matt.forgehax.Globals;
import com.matt.forgehax.util.SafeConverter;
import com.matt.forgehax.util.command.Command;
import com.matt.forgehax.util.command.CommandBuilders;
import com.matt.forgehax.util.mod.CommandMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CMoveVehiclePacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.util.math.Vec3d;

import static com.matt.forgehax.Globals.*;

/**
 * Created by Babbaj on 4/12/2018.
 */
@RegisterMod
public class ClipCommand extends CommandMod {
  
  public ClipCommand() {
    super("ClipCommand");
  }
  
  // teleport to absolute position
  private void setPosition(double x, double y, double z) {
    Entity ent = Globals.getMountedEntityOrPlayer();
    ent.setPositionAndUpdate(x, y, z);

    if(ent instanceof ClientPlayerEntity) {
      Globals.sendNetworkPacket(new CPlayerPacket.PositionPacket(
          ent.getPosX(), ent.getPosY(), ent.getPosZ(), ent.onGround));
    } else {
      Globals.sendNetworkPacket(new CMoveVehiclePacket(ent));
    }
  }
  
  // teleport vertically by some offset
  private void offsetY(double yOffset) {
    Entity local = Globals.getMountedEntityOrPlayer();
    setPosition(local.getPosX(), local.getPosY() + yOffset, local.getPosZ());
  }
  
  @RegisterCommand
  public Command clip(CommandBuilders builders) {
    return builders
      .newCommandBuilder()
      .name("clip")
      .description("Teleport by offset")
      .processor(
        data -> {
          try {
            switch (data.getArgumentCount()) {
              case 1: {
                final double y = Double.parseDouble(data.getArgumentAsString(0));
                addScheduledTask(() -> {
                  if (isInWorld()) {
                    return;
                  }
                  
                  Entity local = getMountedEntityOrPlayer();
                  setPosition(0, local.getPosY() + y, 0);
                });
                break;
              }
              case 3: {
                final double x = Double.parseDouble(data.getArgumentAsString(0));
                final double y = Double.parseDouble(data.getArgumentAsString(1));
                final double z = Double.parseDouble(data.getArgumentAsString(2));
                addScheduledTask(() -> {
                  if (isInWorld()) {
                    return;
                  }

                  Entity local = getMountedEntityOrPlayer();
                  setPosition(local.getPosX() + x, local.getPosY() + y, local.getPosZ() + z);
                });
                break;
              }
              default:
                printError("Invalid number of arguments: expected 1 or 3");
            }
          } catch (NumberFormatException e) {
            printError("Failed to parse input");
          }
        })
      .build();
  }
  
  @RegisterCommand
  public Command vclip(CommandBuilders builders) {
    return builders.newCommandBuilder()
      .name("vclip")
      .description("Vertical clip")
      .requiredArgs(1)
      .processor(data -> {
        if (getWorld() == null || getLocalPlayer() == null) {
          printWarning("Not in game");
          return;
        }
        final double y = SafeConverter.toDouble(data.getArgumentAsString(0));
        addScheduledTask(() -> offsetY(y));
      })
      .build();
  }
  
  @RegisterCommand
  public Command forward(CommandBuilders builders) {
    return builders.newCommandBuilder()
      .name("forward")
      .description("Forward clip")
      .requiredArgs(1)
      .processor(data -> {
        if (getWorld() == null || getLocalPlayer() == null) {
          printWarning("Not in game");
          return;
        }
        final double units = SafeConverter.toDouble(data.getArgumentAsString(0));
        addScheduledTask(() -> {
          Vec3d dir = getLocalPlayer().getLookVec().normalize();
          setPosition(getLocalPlayer().getPosX() + (dir.x * units),
              getLocalPlayer().getPosY(),
              getLocalPlayer().getPosZ() + (dir.z * units));
        });
      })
      .build();
  }
}
