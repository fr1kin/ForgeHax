package com.matt.forgehax.mods.commands;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getNetworkManager;
import static com.matt.forgehax.Helper.getRidingOrPlayer;
import static com.matt.forgehax.Helper.getWorld;
import static com.matt.forgehax.Helper.printWarning;

import com.matt.forgehax.Helper;
import com.matt.forgehax.util.SafeConverter;
import com.matt.forgehax.util.command.Command;
import com.matt.forgehax.util.command.CommandBuilders;
import com.matt.forgehax.util.mod.CommandMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketVehicleMove;
import net.minecraft.util.math.Vec3d;

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
    final Entity local = Helper.getRidingOrPlayer();
    local.setPositionAndUpdate(x, y, z);
    if (local instanceof EntityPlayerSP) {
      getNetworkManager()
        .sendPacket(
          new CPacketPlayer.Position(local.posX, local.posY, local.posZ, MC.player.onGround));
    } else {
      getNetworkManager().sendPacket(new CPacketVehicleMove(local));
    }
  }

  // teleport vertically by some offset
  private void offsetY(double yOffset) {
    Entity local = Helper.getRidingOrPlayer();
    setPosition(local.posX, local.posY + yOffset, local.posZ);
  }

  @RegisterCommand
  public Command clip(CommandBuilders builders) {
    return builders
      .newCommandBuilder()
      .name("clip")
      .description("Teleport vertically")
      // .requiredArgs(1)
      .processor(
        data -> {
          try {
            switch (data.getArgumentCount()) {
              case 1: {
                final double y = Double.parseDouble(data.getArgumentAsString(0));
                MC.addScheduledTask(() -> {
                  if (getWorld() == null || getLocalPlayer() == null) {
                    return;
                  }
              
                  Entity local = getRidingOrPlayer();
                  if (local == null) {
                    return;
                  }
              
                  setPosition(0, local.posY + y, 0);
                });
                break;
              }
              case 3: {
                final double x = Double.parseDouble(data.getArgumentAsString(0));
                final double y = Double.parseDouble(data.getArgumentAsString(1));
                final double z = Double.parseDouble(data.getArgumentAsString(2));
                MC.addScheduledTask(() -> {
                  if (getWorld() == null || getLocalPlayer() == null) {
                    return;
                  }
              
                  Entity local = getRidingOrPlayer();
                  if (local == null) {
                    return;
                  }
              
                  setPosition(local.posX + x, local.posY + y, local.posZ + z);
                });
                break;
              }
              default:
                Helper.printMessage("Invalid number of arguments: expected 1 or 3");
            }
          } catch (NumberFormatException e) {
            Helper.printMessage("Failed to parse input");
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
        MC.addScheduledTask(() -> offsetY(y));
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
        MC.addScheduledTask(() -> {
          Vec3d dir = getLocalPlayer().getLookVec().normalize();
          setPosition(getLocalPlayer().posX + (dir.x * units), getLocalPlayer().posY,
            getLocalPlayer().posZ + (dir.z * units));
        });
      })
      .build();
  }
}
