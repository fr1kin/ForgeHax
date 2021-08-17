package dev.fiki.forgehax.main.mods.player;

import dev.fiki.forgehax.api.asm.MapField;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.entity.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.api.reflection.types.ReflectionField;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.entity.player.ClientPlayerEntity;

import static dev.fiki.forgehax.main.Common.getLocalPlayer;

@RegisterMod(
    name = "HorseJump",
    description = "Always max horse jump",
    category = Category.PLAYER
)
@RequiredArgsConstructor
public class HorseJump extends ToggleMod {
  @MapField(parentClass = ClientPlayerEntity.class, value = "jumpRidingScale")
  public final ReflectionField<Float> ClientPlayerEntity_jumpRidingScale;

  @SubscribeListener
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    ClientPlayerEntity_jumpRidingScale.set(getLocalPlayer(), 1.F);
  }
}
