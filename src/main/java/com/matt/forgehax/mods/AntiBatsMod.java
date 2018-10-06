package com.matt.forgehax.mods;

import com.google.common.eventbus.Subscribe;
import com.matt.forgehax.asm.events.replacementhooks.PlaySoundAtEntityEvent;
import com.matt.forgehax.asm.events.replacementhooks.RenderLivingEvent;
import com.matt.forgehax.util.common.PriorityEnum;
import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.entity.mobtypes.MobType;
import com.matt.forgehax.util.entity.mobtypes.MobTypeEnum;
import com.matt.forgehax.util.entity.mobtypes.MobTypeRegistry;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.init.SoundEvents;

@RegisterMod
public class AntiBatsMod extends ToggleMod {
    private static final MobType BATS_MOBTYPE = new MobType() {
        @Override
        protected PriorityEnum getPriority() {
            return PriorityEnum.LOW;
        }

        @Override
        public boolean isMobType(Entity entity) {
            return entity instanceof EntityBat;
        }

        @Override
        protected MobTypeEnum getMobTypeUnchecked(Entity entity) {
            return MobTypeEnum.INVALID;
        }
    };

    public AntiBatsMod() {
        super(Category.RENDER, "AntiBats", false, "666 KILL BATS 666");
    }

    @Override
    public void onEnabled() {
        MobTypeRegistry.register(BATS_MOBTYPE);
        EntityUtils.isBatsDisabled = true;
    }

    @Override
    public void onDisabled() {
        MobTypeRegistry.unregister(BATS_MOBTYPE);
        EntityUtils.isBatsDisabled = false;
    }

    @Subscribe
    public void onRenderLiving(RenderLivingEvent.Pre event) {
        if(event.getEntity() instanceof EntityBat)
            event.setCanceled(true);
    }

    @Subscribe
    public void onPlaySound(PlaySoundAtEntityEvent event) {
        if(event.getSound().equals(SoundEvents.ENTITY_BAT_AMBIENT) ||
                event.getSound().equals(SoundEvents.ENTITY_BAT_DEATH) ||
                event.getSound().equals(SoundEvents.ENTITY_BAT_HURT) ||
                event.getSound().equals(SoundEvents.ENTITY_BAT_LOOP) ||
                event.getSound().equals(SoundEvents.ENTITY_BAT_TAKEOFF))
            event.setCanceled(true);
    }
}
