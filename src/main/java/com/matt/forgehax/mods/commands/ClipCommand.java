package com.matt.forgehax.mods.commands;

import com.matt.forgehax.Helper;
import com.matt.forgehax.util.command.Command;
import com.matt.forgehax.util.command.CommandBuilders;
import com.matt.forgehax.util.mod.CommandMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketVehicleMove;

import static com.matt.forgehax.Helper.getNetworkManager;

/**
 * Created by Babbaj on 4/12/2018.
 */
@RegisterMod
public class ClipCommand extends CommandMod {

    public ClipCommand() {
        super("ClipCommand");
    }

    // teleport to absolute position
    private void teleport(double x, double y, double z) {
        final Entity local =  Helper.getRidingOrPlayer();
        local.setPositionAndUpdate(x,  y, z);
        if (local instanceof EntityPlayerSP) {
            getNetworkManager().sendPacket(new CPacketPlayer.Position(local.posX, local.posY, local.posZ, MC.player.onGround));
        } else {
            getNetworkManager().sendPacket(new CPacketVehicleMove(local));
        }
    }

    // teleport vertically by some offset
    private void teleport(double yOffset) {
        Entity local = Helper.getRidingOrPlayer();
        teleport(local.posX, local.posY + yOffset, local.posZ);
    }

    @RegisterCommand
    public Command clip(CommandBuilders builders) {
        return builders.newCommandBuilder()
                .name("clip")
                .description("Teleport vertically")
                //.requiredArgs(1)
                .processor(data -> {
                    try {
                        switch (data.getArgumentCount()) {
                            case 1: teleport(Double.parseDouble(data.getArgumentAsString(0)));
                                break;
                            case 3: teleport(
                                    Double.parseDouble(data.getArgumentAsString(0)),
                                    Double.parseDouble(data.getArgumentAsString(1)),
                                    Double.parseDouble(data.getArgumentAsString(2)));
                                break;
                            default:
                                Helper.printMessage("Invalid number of arguments: expected 1 or 3");
                        }
                    } catch (NumberFormatException e) {
                        Helper.printMessage("Failed to parse input");
                    }
                })
                .build();
    }
}
