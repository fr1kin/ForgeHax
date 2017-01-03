package com.matt.forgehax.mods;

import com.matt.forgehax.ForgeHax;
import com.matt.forgehax.asm.events.PacketEvent;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Scanner;

public class TeleportMod extends ToggleMod {
    public Property mode;
    public Property relative;

    public TeleportMod(String modName, boolean defaultValue, String description, int key) {
        super(modName, defaultValue, description, key);
    }

    @Override
    public void loadConfig(Configuration configuration) {
        addSettings(
                mode = configuration.get(getModName(),
                        "teleport_mode",
                        0,
                        "Packet teleport types",
                        0, 5
                ),
                relative = configuration.get(getModName(),
                        "teleport_relative",
                        false,
                        ".setpos will tp you relative to your current position"
                )
        );
    }

    @SubscribeEvent
    public void onPacketSent(PacketEvent.Send.Pre event) {
        if(event.getPacket() instanceof CPacketChatMessage) {
            String message = ((CPacketChatMessage) event.getPacket()).getMessage();
            Scanner scanner = new Scanner(message);
            scanner.useDelimiter(" ");
            if(scanner.next().equals(".setpos")) {
                double x = 0, y = 0, z = 0;
                boolean onGround = true;
                try {
                    x = Double.parseDouble(scanner.next());
                    y = Double.parseDouble(scanner.next());
                    z = Double.parseDouble(scanner.next());
                    if(scanner.hasNext()) {
                        try {
                            onGround = Boolean.parseBoolean(scanner.next());
                        } catch (Exception e) {
                            ForgeHax.instance().printStackTrace(e);
                        }
                    }
                    if(relative.getBoolean()) {
                        Vec3d pos = getLocalPlayer().getPositionVector();
                        x = pos.xCoord + x;
                        y = pos.yCoord + y;
                        z = pos.zCoord + z;
                    }
                    switch (mode.getInt()) {
                        default:
                        case 0:
                            if(getLocalPlayer().isRiding() &&
                                    getLocalPlayer().getRidingEntity() != null) {
                                getLocalPlayer().getRidingEntity().setPosition(x ,y ,z);
                            } else {
                                getLocalPlayer().setPosition(x, y, z);
                            }
                            break;
                        case 1:
                            getNetworkManager().sendPacket(new CPacketPlayer.Position(x, y, z, onGround));
                            break;
                        case 2:
                            getNetworkManager().sendPacket(new CPacketConfirmTeleport());
                    }
                    MC.player.sendChatMessage("Attempted teleport using mode " + mode.getInt());
                } catch (Exception e) {
                    ForgeHax.instance().printStackTrace(e);
                }
                event.setCanceled(true);
            }
        }
    }
}
