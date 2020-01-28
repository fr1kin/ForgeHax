package com.matt.forgehax.mods;

import com.matt.forgehax.Globals;
import com.matt.forgehax.events.ClientTickEvent;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.stream.StreamSupport;

import static com.matt.forgehax.Globals.*;

@RegisterMod
public class NoRender extends ToggleMod {
  
  public NoRender() {
    super(Category.RENDER, "NoRender", false, "Stops rendering items on ground");
  }
  
  @SubscribeEvent
  public void onClientTick(ClientTickEvent.Pre event) {
    if (isInWorld()) {
      return;
    }

    StreamSupport.stream(getWorld().getAllEntities().spliterator(), false)
        .filter(ItemEntity.class::isInstance)
        .map(ItemEntity.class::cast)
        .map(Entity::getEntityId)
        .forEach(getWorld()::removeEntityFromWorld);
  }
}

