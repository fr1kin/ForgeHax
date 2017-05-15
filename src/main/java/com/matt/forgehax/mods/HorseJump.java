package com.matt.forgehax.mods;

import com.matt.forgehax.Wrapper;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.key.Bindings;
import net.minecraft.util.datafix.fixes.HorseSaddle;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.relauncher.ReflectionHelper;


public class HorseJump extends ToggleMod {


    public HorseJump() {
        super("HorseJump", false, "always max horse jump");
    }


    @SubscribeEvent
	public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    	//MC.player.horseJumpPower = 1.0F;// right now the enviorment is non obfuscated


        ReflectionHelper.setPrivateValue(EntityPlayerSP.class,
                Wrapper.getLocalPlayer(),
                1.0F,
                "horseJumpPower", "field_110321_bQ");
        // when u see something like fieldNames... that is var args which is pretty much an array
        // which would be new String[] {"horseJumpPower", "field_110321_bQ"}
    }
}
