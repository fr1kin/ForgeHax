package com.matt.forgehax;

import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.events.listeners.WorldListener;
import com.matt.forgehax.mods.BaseMod;
import com.matt.forgehax.util.Angle;
import com.matt.forgehax.util.PlayerUtils;
import com.matt.forgehax.util.ProjectileUtils;
import com.matt.forgehax.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

import java.util.Map;

public class ForgeHaxEventHandler extends ForgeHaxBase {
    private static final WorldListener WORLD_LISTENER = new WorldListener();

    /**
     * Called when the local player updates
     */
    @SubscribeEvent
    public void onUpdate(LivingEvent.LivingUpdateEvent event) {
        if(MC.theWorld != null &&
                event.getEntityLiving().equals(MC.thePlayer)) {
            Event ev = new LocalPlayerUpdateEvent(event.getEntityLiving());
            MinecraftForge.EVENT_BUS.post(ev);
            event.setCanceled(ev.isCanceled());
        } else if(event.getEntityLiving() instanceof EntityPigZombie) {
            // update pigmens anger level
            if(((EntityPigZombie) event.getEntityLiving()).isAngry())
                --((EntityPigZombie) event.getEntity()).angerLevel;
        }
    }

    /**
     * For the world listener (adding/removing entity events)
     */
    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        event.getWorld().addEventListener(WORLD_LISTENER);
    }

    /**
     * Mod key bind handling
     */
    @SubscribeEvent
    public void onKeyboardEvent(InputEvent.KeyInputEvent event) {
        for(Map.Entry<String,BaseMod> entry : MOD.mods.entrySet()) {
            for(KeyBinding bind : entry.getValue().getKeyBinds()) {
                if(bind.isPressed())
                    entry.getValue().onBindPressed(bind);
                if(bind.isKeyDown())
                    entry.getValue().onBindKeyDown(bind);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSentPacket(PacketEvent.SendEvent.Post event) {
        if(Utils.OUTGOING_PACKET_IGNORE_LIST.contains(event.getPacket())) {
            // remove packet from list (we wont be seeing it ever again)
            Utils.OUTGOING_PACKET_IGNORE_LIST.remove(event.getPacket());
        }
    }
}
