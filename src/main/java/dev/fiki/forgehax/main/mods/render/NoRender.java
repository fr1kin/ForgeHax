package dev.fiki.forgehax.main.mods.render;

import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.game.PreGameTickEvent;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;

import static dev.fiki.forgehax.main.Common.*;

@RegisterMod(
    name = "NoRender",
    description = "Stops rendering items on ground",
    category = Category.RENDER
)
public class NoRender extends ToggleMod {
  @SubscribeListener
  public void onClientTick(PreGameTickEvent event) {
    if (isInWorld()) {
      worldEntities()
          .filter(ItemEntity.class::isInstance)
          .map(ItemEntity.class::cast)
          .map(Entity::getId)
          .forEach(getWorld()::removeEntity);
    }
  }
}

