package com.matt.forgehax.mods;

import com.matt.forgehax.Helper;
import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import static com.matt.forgehax.Helper.getNetworkManager;
import static com.matt.forgehax.Helper.printMessage;

/**
 * Created by Babbaj on 9/1/2017.
 */
@RegisterMod
public class AntiSetSlotMod extends ToggleMod {
    public AntiSetSlotMod() {
        super("AntiSetSlot", false, "prevents the server from changing selected hotbar slot");
    }

    @SubscribeEvent
    public void onPacketReceived(PacketEvent.Incoming.Pre event) {
        if (event.getPacket() instanceof SPacketSetSlot) {
            int currentSlot = MC.player.inventory.currentItem;

            if (((SPacketSetSlot) event.getPacket()).getSlot() != currentSlot) {
                getNetworkManager().sendPacket(new CPacketHeldItemChange(currentSlot)); // set server's slot back to our slot
                FastReflection.Methods.KeyBinding_unPress.invoke(MC.gameSettings.keyBindAttack); // likely will eating so stop right clicking

                printMessage("Server attempted to change hotbar slot");
                event.setCanceled(true);
            }
        }
    }


}
