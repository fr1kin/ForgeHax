package com.matt.forgehax.mods;

import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.matt.forgehax.Helper.getRidingEntity;

/**
 * Created by Babbaj on 9/1/2017.
 */
@RegisterMod
public class HorseStats extends ToggleMod {
    public HorseStats() { super(Category.PLAYER, "HorseStats", false, "Change the stats of your horse"); }

    public final Setting<Double> jumpHeight = getCommandStub().builders().<Double>newSettingBuilder()
            .name("JumpHeight").description("Modified horse jump height attribute. Default: 1")
            .defaultTo(1.0D).build();
    public final Setting<Double> speed = getCommandStub().builders().<Double>newSettingBuilder()
            .name("Speed").description("Modiified horse speed attribute. Default: 0.3375")
            .defaultTo(0.3375D).build();

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (EntityUtils.isDrivenByPlayer(event.getEntity()) && getRidingEntity() instanceof AbstractHorse) {

            IAttribute JUMP_STRENGTH = FastReflection.Fields.AbstractHorse_JUMP_STRENGTH.get(getRidingEntity());
            IAttribute MOVEMENT_SPEED = FastReflection.Fields.SharedMonsterAttributes_MOVEMENT_SPEED.get(getRidingEntity());

            ((EntityLivingBase) getRidingEntity()).getEntityAttribute(JUMP_STRENGTH).setBaseValue(jumpHeight.getAsDouble());
            ((EntityLivingBase) getRidingEntity()).getEntityAttribute(MOVEMENT_SPEED).setBaseValue(speed.getAsDouble());
        }
    }

}
