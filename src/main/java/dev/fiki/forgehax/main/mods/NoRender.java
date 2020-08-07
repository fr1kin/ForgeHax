package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.events.PreClientTickEvent;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.stream.StreamSupport;

import static dev.fiki.forgehax.main.Common.getWorld;
import static dev.fiki.forgehax.main.Common.isInWorld;

@RegisterMod(
    name = "NoRender",
    description = "Stops rendering items on ground",
    category = Category.RENDER
)
public class NoRender extends ToggleMod {
  
  @SubscribeEvent
  public void onClientTick(PreClientTickEvent event) {
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

