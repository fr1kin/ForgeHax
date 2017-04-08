package com.matt.forgehax.mods;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketCreativeInventoryAction;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created on 2/10/2017 by fr1kin
 */
public class FloodMaps extends ToggleMod {
    public FloodMaps(String modName, boolean defaultValue, String description, int key) {
        super(modName, defaultValue, description, key);
    }

    @SubscribeEvent
    public void onUpdate(LocalPlayerUpdateEvent event) {
        if(MC.currentScreen instanceof GuiInventory) {
            int mapSlot = 0;
            for(ItemStack stack : getLocalPlayer().inventory.mainInventory) {

                mapSlot++;
            }
        }
    }
}
