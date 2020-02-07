package dev.fiki.forgehax.main.mods.commands;

import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.cmd.argument.Arguments;
import dev.fiki.forgehax.main.util.cmd.flag.EnumFlag;
import dev.fiki.forgehax.main.util.mod.CommandMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CMoveVehiclePacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.util.math.Vec3d;

import static dev.fiki.forgehax.main.Common.*;

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
    Entity ent = getMountedEntityOrPlayer();
    ent.setPositionAndUpdate(x, y, z);

    if (ent instanceof ClientPlayerEntity) {
      sendNetworkPacket(new CPlayerPacket.PositionPacket(
          ent.getPosX(), ent.getPosY(), ent.getPosZ(), ent.onGround));
    } else {
      sendNetworkPacket(new CMoveVehiclePacket(ent));
    }
  }

  // teleport vertically by some offset
  private void offsetY(double yOffset) {
    Entity local = getMountedEntityOrPlayer();
    setPosition(local.getPosX(), local.getPosY() + yOffset, local.getPosZ());
  }

  {
    newSimpleCommand()
        .name("clip")
        .description("Teleport by offset")
        .argument(Arguments.newDoubleArgument()
            .label("x")
            .defaultValue(0.D)
            .build())
        .argument(Arguments.newDoubleArgument()
            .label("y")
            .defaultValue(0.D)
            .build())
        .argument(Arguments.newDoubleArgument()
            .label("z")
            .defaultValue(0.D)
            .build())
        .flag(EnumFlag.EXECUTOR_MAIN_THREAD)
        .executor(args -> {
          if (!isInWorld()) {
            args.error("Not in world.");
            return;
          }

          double x = args.<Double>getFirst().getValueOrDefault();
          double y = args.<Double>getSecond().getValueOrDefault();
          double z = args.<Double>getThird().getValueOrDefault();

          Entity local = getMountedEntityOrPlayer();
          setPosition(local.getPosX() + x, local.getPosY() + y, local.getPosZ() + z);
        })
        .build();
  }

  {
    newSimpleCommand()
        .name("vclip")
        .description("Vertical clip")
        .argument(Arguments.newDoubleArgument()
            .label("y")
            .build())
        .flag(EnumFlag.EXECUTOR_MAIN_THREAD)
        .executor(args -> {
          if (!isInWorld()) {
            args.error("Not in world.");
            return;
          }

          double y = args.<Double>getFirst().getValueOrDefault();
          offsetY(y);
        })
        .build();
  }

  {
    newSimpleCommand()
        .name("forward")
        .description("Forward clip")
        .argument(Arguments.newDoubleArgument()
            .label("offset")
            .build())
        .flag(EnumFlag.EXECUTOR_MAIN_THREAD)
        .executor(args -> {
          if (!isInWorld()) {
            args.error("Not in world.");
            return;
          }

          double offset = args.<Double>getFirst().getValueOrDefault();

          Vec3d dir = getLocalPlayer().getLookVec().normalize();
          setPosition(getLocalPlayer().getPosX() + (dir.x * offset),
              getLocalPlayer().getPosY(),
              getLocalPlayer().getPosZ() + (dir.z * offset));
        })
        .build();
  }
}
