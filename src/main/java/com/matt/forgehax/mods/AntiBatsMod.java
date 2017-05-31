package com.matt.forgehax.mods;

import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.init.SoundEvents;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class AntiBatsMod extends ToggleMod {
    public AntiBatsMod() {
        super("AntiBats", false, "666 KILL BATS 666");
    }

    @Override
    public void onEnabled() {
        EntityUtils.isBatsDisabled = true;
    }

    @Override
    public void onDisabled() {
        EntityUtils.isBatsDisabled = false;
    }

    @SubscribeEvent
    public void onRenderLiving(RenderLivingEvent.Pre<?> event) {
        if(event.getEntity() instanceof EntityBat)
            event.setCanceled(true);
    }

    @SubscribeEvent
    public void onPlaySound(PlaySoundAtEntityEvent event) {
        if(event.getSound().equals(SoundEvents.ENTITY_BAT_AMBIENT) ||
                event.getSound().equals(SoundEvents.ENTITY_BAT_DEATH) ||
                event.getSound().equals(SoundEvents.ENTITY_BAT_HURT) ||
                event.getSound().equals(SoundEvents.ENTITY_BAT_LOOP) ||
                event.getSound().equals(SoundEvents.ENTITY_BAT_TAKEOFF))
            event.setCanceled(true);
    }
}
