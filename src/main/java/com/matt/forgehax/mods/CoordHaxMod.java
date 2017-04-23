package com.matt.forgehax.mods;

import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.mods.data.PacketCache;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

/**
 * Created on 12/3/2016 by fr1kin
 */
public class CoordHaxMod extends ToggleMod {
    public static File outputFile = null;

    public Property logIgnoreRadius;
    public Property rate;

    private boolean shouldBeSending = false;
    private Packet selectedShulkerBoxOpenPacket = null;

    public CoordHaxMod() {
        super("CoordLogger", false, "hax");
    }

    @Override
    public void loadConfig(Configuration configuration) {
        addSettings(
                logIgnoreRadius = configuration.get(getModName(),
                        "log_ignore_radius",
                        64,
                        "Radius to ignore BlockPos packets"
                ),
                rate = configuration.get(getModName(),
                        "send_rate",
                        1,
                        "Rate at which to send gui open packets"
                )
        );
    }

    @Override
    public void onDisabled() {
        tickCount = 0;
    }

    public void log(String fmt, Object... args) {
        if(outputFile == null) {
            outputFile = new File(MOD.getBaseDirectory(), "coord_log.txt");
        }
        if(!outputFile.exists()) {
            try {
                Files.createFile(outputFile.toPath());
            } catch (Exception e) {
                MOD.printStackTrace(e);
            }
        }
        String str = String.format(fmt, args);
        try {
            Files.write(outputFile.toPath(), str.getBytes(), StandardOpenOption.APPEND);
        } catch (Exception e) {
            MOD.printStackTrace(e);
        }
    }

    public void addChatMsg(String str, Object... args) {
        if(MC.player != null) {
            //MC.player.sendChatMessage(String.format(str, args));
        }
    }

    @SubscribeEvent
    public void onKeyInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if(Keyboard.getEventKey() == Keyboard.KEY_H &&
                Keyboard.isKeyDown(Keyboard.KEY_H)) {
            shouldBeSending = !shouldBeSending;
            if(!shouldBeSending) {
                selectedShulkerBoxOpenPacket = null;
                tickCount = 0;
                addChatMsg("Stopped sending packets");
            } else {
                addChatMsg("Started sending packets");
            }
        }
    }

    private long tickCount = 0;

    @SubscribeEvent
    public void onUpdate(LocalPlayerUpdateEvent event) {
        tickCount++;
        if((tickCount % rate.getInt()) == 0 &&
                shouldBeSending &&
                selectedShulkerBoxOpenPacket != null) {
            WRAPPER.getNetworkManager().sendPacket(selectedShulkerBoxOpenPacket);
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Outgoing.Pre event) {
        if(WRAPPER.getWorld() != null &&
                selectedShulkerBoxOpenPacket == null &&
                event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {
            IBlockState state = WRAPPER.getWorld().getBlockState(((CPacketPlayerTryUseItemOnBlock) event.getPacket()).getPos());
            if(state.getBlock() instanceof BlockShulkerBox) {
                selectedShulkerBoxOpenPacket = event.getPacket();
            }
        }
    }

    @SubscribeEvent
    public void onPacketRecieved(PacketEvent.Incoming.Pre event) {
        if(MC.player != null) {
            BlockPos myPos = MC.player.getPosition();
            for (Field field : PacketCache.getBlockPosFields(event.getPacket())) {
                try {
                    BlockPos pos = (BlockPos) field.get(event.getPacket());
                    int logRadius = logIgnoreRadius.getInt();
                    if (pos.distanceSq(myPos) > (Math.pow(logRadius, 2))) {
                        log("[%s] BlockPos out of bounds (%dm): %d, %d, %d\n",
                                event.getPacket().getClass().getSimpleName(),
                                logRadius,
                                pos.getX(), pos.getY(), pos.getZ()
                        );
                    }
                } catch (Exception e) {
                    MOD.printStackTrace(e);
                }
            }
        }
    }
}
