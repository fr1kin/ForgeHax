package dev.fiki.forgehax.main.commands;

import dev.fiki.forgehax.api.cmd.argument.Arguments;
import dev.fiki.forgehax.api.cmd.flag.EnumFlag;
import dev.fiki.forgehax.api.mod.CommandMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.api.reflection.ReflectionTools;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CMoveVehiclePacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.util.math.vector.Vector3d;

import static dev.fiki.forgehax.main.Common.*;

/**
 * Created by Babbaj on 4/12/2018.
 */
@RegisterMod
@RequiredArgsConstructor
public class ClipCommand extends CommandMod {
  private final ReflectionTools reflection;

  // Entity::setPositionAndUpdate has a pozzed check
  private static void mcSetPositionAndUpdate(Entity ent, double x, double y, double z) {
    ent.moveTo(x, y, z, ent.yRot, ent.xRot);
    // update passengers
    ent.getSelfAndPassengers().forEach(e -> {
      //p_226267_1_.isPositionDirty = true;
      e.moveTo(x, y, z); // todo: 1.16 idk if this is correct
    });
  }

  // teleport to absolute position
  private void setPosition(Entity ent, double x, double y, double z) {
    mcSetPositionAndUpdate(ent, x, y, z);

    if (ent instanceof ClientPlayerEntity) {
      sendNetworkPacket(new CPlayerPacket.PositionPacket(
          ent.getX(), ent.getY(), ent.getZ(), reflection.Entity_onGround.get(ent)));
    } else {
      sendNetworkPacket(new CMoveVehiclePacket(ent));
    }
  }

  // teleport vertically by some offset
  private void offsetY(double yOffset) {
    Entity local = getMountedEntityOrPlayer();
    setPosition(local, local.getX(), local.getY() + yOffset, local.getZ());
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

          double x = args.<Double>getFirst().getValue();
          double y = args.<Double>getSecond().getValue();
          double z = args.<Double>getThird().getValue();

          Entity local = getMountedEntityOrPlayer();
          setPosition(local, local.getX() + x, local.getY() + y, local.getZ() + z);
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
        //.flag(EnumFlag.EXECUTOR_MAIN_THREAD)
        .executor(args -> {
          if (!isInWorld()) {
            args.error("Not in world.");
            return;
          }

          double y = args.<Double>getFirst().getValue();
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

          double offset = args.<Double>getFirst().getValue();

          Vector3d dir = getLocalPlayer().getForward().normalize();
          final Entity local = getLocalPlayer();
          setPosition(local, local.getX() + (dir.x * offset),
              local.getY(),
              local.getZ() + (dir.z * offset));
        })
        .build();
  }
}
