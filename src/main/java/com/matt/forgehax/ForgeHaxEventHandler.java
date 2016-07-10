package com.matt.forgehax;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.events.listeners.WorldListener;
import com.matt.forgehax.mods.BaseMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

import java.util.Map;

public class ForgeHaxEventHandler {
    protected static final Minecraft mc = Minecraft.getMinecraft();
    protected static final ForgeHax mod = ForgeHax.instance();

    private static final WorldListener WORLD_LISTENER = new WorldListener();

    /**
     * Called when the local player updates
     */
    @SubscribeEvent
    public void onUpdate(LivingEvent.LivingUpdateEvent event) {
        if(mc.theWorld != null &&
                event.getEntityLiving().equals(mc.thePlayer)) {
            Event ev = new LocalPlayerUpdateEvent(event.getEntityLiving());
            MinecraftForge.EVENT_BUS.post(ev);
            event.setCanceled(ev.isCanceled());
        } else if(event.getEntityLiving() instanceof EntityPigZombie) {
            // update pigmens anger level
            if(((EntityPigZombie) event.getEntityLiving()).isAngry())
                --((EntityPigZombie) event.getEntity()).angerLevel;
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        event.getWorld().addEventListener(WORLD_LISTENER);
    }

    @SubscribeEvent
    public void onKeyboardEvent(InputEvent.KeyInputEvent event) {
        for(Map.Entry<String,BaseMod> entry : mod.mods.entrySet()) {
            for(KeyBinding bind : entry.getValue().getKeyBinds()) {
                if(bind.isPressed())
                    entry.getValue().onBindPressed(bind);
                if(bind.isKeyDown())
                    entry.getValue().onBindKeyDown(bind);
            }
        }
    }
}
