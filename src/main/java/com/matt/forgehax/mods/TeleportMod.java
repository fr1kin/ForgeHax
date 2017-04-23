package com.matt.forgehax.mods;

import com.matt.forgehax.ForgeHax;
import com.matt.forgehax.asm.events.PacketEvent;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Scanner;

public class TeleportMod extends ToggleMod {
    public Property mode;
    public Property relative;

    public TeleportMod() {
        super("Teleport", false, "Type '.setpos [x] [y] [z] [onGround]' in chat to use");
    }

    @Override
    public void loadConfig(Configuration configuration) {
        addSettings(mode = configuration.get(getModName(), "teleport_mode", 0, "Packet teleport types", 0, 5), relative = configuration.get(getModName(), "teleport_relative", false, ".setpos will tp you relative to your current position"));
    }

    @SubscribeEvent
    public void onPacketSent(PacketEvent.Outgoing.Pre event) {
        if (event.getPacket() instanceof CPacketChatMessage) {
            String message = ((CPacketChatMessage) event.getPacket()).getMessage();
            Scanner scanner = new Scanner(message);
            scanner.useDelimiter(" ");
            if (scanner.next().equals(".setpos")) {
                double x = 0, y = 0, z = 0;
                boolean onGround = true;
                try {
                    x = Double.parseDouble(scanner.next());
                    y = Double.parseDouble(scanner.next());
                    z = Double.parseDouble(scanner.next());
                    if (scanner.hasNext()) {
                        try {
                            onGround = Boolean.parseBoolean(scanner.next());
                        } catch (Exception e) {
                            ForgeHax.getInstance().printStackTrace(e);
                        }
                    }
                    if (relative.getBoolean()) {
                        Vec3d pos = WRAPPER.getLocalPlayer().getPositionVector();
                        x = pos.xCoord + x;
                        y = pos.yCoord + y;
                        z = pos.zCoord + z;
                    }
                    switch (mode.getInt()) {
                        default:
                        case 0:
                            if (WRAPPER.getLocalPlayer().isRiding() && WRAPPER.getLocalPlayer().getRidingEntity() != null) {
                                WRAPPER.getLocalPlayer().getRidingEntity().setPosition(x, y, z);
                            } else {
                                WRAPPER.getLocalPlayer().setPosition(x, y, z);
                            }
                            break;
                        case 1:
                            WRAPPER.getNetworkManager().sendPacket(new CPacketPlayer.Position(x, y, z, onGround));
                            break;
                        case 2:
                            WRAPPER.getNetworkManager().sendPacket(new CPacketConfirmTeleport());
                            break;
                        case 3:
                            if (WRAPPER.getLocalPlayer().getRidingEntity() != null) {
                                WRAPPER.getLocalPlayer().getRidingEntity().setEntityBoundingBox(WRAPPER.getLocalPlayer().getRidingEntity().getEntityBoundingBox().offset(x, y, z));
                            } else {
                                WRAPPER.getLocalPlayer().setEntityBoundingBox(WRAPPER.getLocalPlayer().getEntityBoundingBox().offset(x, y, z));
                            }
                            break;
                        case 4:
                            MC.player.setPosition(MC.player.posX + (1F * -Double.NaN * x + 0F * -Double.NaN * z), MC.player.posY, MC.player.posZ + (1F * -Double.NaN * z - 0F * -Double.NaN * x));
                            break;
                        case 5:
                            for (int i = 0; i < z; ++i) {
                                MC.player.connection.sendPacket(new CPacketPlayer.Position(MC.player.posX, MC.player.posY + 0.049, MC.player.posZ, false));
                                MC.player.connection.sendPacket(new CPacketPlayer.Position(MC.player.posX, MC.player.posY, MC.player.posZ, false));
                            }
                            MC.player.connection.sendPacket(new CPacketPlayer.Position(MC.player.posX, MC.player.posY, MC.player.posZ, false));
                            break;
                    }
                    //getLocalPlayer().sendChatMessage("Attempted teleport using mode " + mode.getInt());
                } catch (Exception e) {
                    ForgeHax.getInstance().printStackTrace(e);
                }
                event.setCanceled(true);
            }
        }
    }
}