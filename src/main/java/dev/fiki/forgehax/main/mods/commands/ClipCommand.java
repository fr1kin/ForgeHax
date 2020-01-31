package dev.fiki.forgehax.main.mods.commands;

import dev.fiki.forgehax.main.Globals;
import dev.fiki.forgehax.main.util.command.Command;
import dev.fiki.forgehax.main.util.command.CommandBuilders;
import dev.fiki.forgehax.main.util.mod.CommandMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.util.SafeConverter;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CMoveVehiclePacket;
import net.minecraft.network.play.client.CPlayerPacket;
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
                Globals.addScheduledTask(() -> {
                  if (Globals.isInWorld()) {
                    return;
                  }
                  
                  Entity local = Globals.getMountedEntityOrPlayer();
                  setPosition(0, local.getPosY() + y, 0);
                });
                break;
              }
              case 3: {
                final double x = Double.parseDouble(data.getArgumentAsString(0));
                final double y = Double.parseDouble(data.getArgumentAsString(1));
                final double z = Double.parseDouble(data.getArgumentAsString(2));
                Globals.addScheduledTask(() -> {
                  if (Globals.isInWorld()) {
                    return;
                  }

                  Entity local = Globals.getMountedEntityOrPlayer();
                  setPosition(local.getPosX() + x, local.getPosY() + y, local.getPosZ() + z);
                });
                break;
              }
              default:
                Globals.printError("Invalid number of arguments: expected 1 or 3");
            }
          } catch (NumberFormatException e) {
            Globals.printError("Failed to parse input");
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
        if (Globals.getWorld() == null || Globals.getLocalPlayer() == null) {
          Globals.printWarning("Not in game");
          return;
        }
        final double y = SafeConverter.toDouble(data.getArgumentAsString(0));
        Globals.addScheduledTask(() -> offsetY(y));
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
        if (Globals.getWorld() == null || Globals.getLocalPlayer() == null) {
          Globals.printWarning("Not in game");
          return;
        }
        final double units = SafeConverter.toDouble(data.getArgumentAsString(0));
        Globals.addScheduledTask(() -> {
          Vec3d dir = Globals.getLocalPlayer().getLookVec().normalize();
          setPosition(Globals.getLocalPlayer().getPosX() + (dir.x * units),
              Globals.getLocalPlayer().getPosY(),
              Globals.getLocalPlayer().getPosZ() + (dir.z * units));
        });
      })
      .build();
  }
}
