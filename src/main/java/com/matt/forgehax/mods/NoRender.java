package com.matt.forgehax.mods;

import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getWorld;

@RegisterMod
public class NoRender extends ToggleMod {

    public NoRender() {
        super(Category.RENDER, "NoRender", false, "Stops rendering items on ground");
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent event) {
        if (getWorld() == null || getLocalPlayer() == null) return;
        getWorld()
                .loadedEntityList
                .stream()
                .filter(EntityItem.class::isInstance)
                .map(EntityItem.class::cast)
                .forEach(Entity::setDead);
    }
}
