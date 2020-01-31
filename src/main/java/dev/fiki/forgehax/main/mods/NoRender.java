package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.Globals;
import dev.fiki.forgehax.main.events.ClientTickEvent;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.stream.StreamSupport;

@RegisterMod
public class NoRender extends ToggleMod {
  
  public NoRender() {
    super(Category.RENDER, "NoRender", false, "Stops rendering items on ground");
  }
  
  @SubscribeEvent
  public void onClientTick(ClientTickEvent.Pre event) {
    if (Globals.isInWorld()) {
      return;
    }

    StreamSupport.stream(Globals.getWorld().getAllEntities().spliterator(), false)
        .filter(ItemEntity.class::isInstance)
        .map(ItemEntity.class::cast)
        .map(Entity::getEntityId)
        .forEach(Globals.getWorld()::removeEntityFromWorld);
  }
}

