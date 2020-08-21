package dev.fiki.forgehax.main.mods.player;

import dev.fiki.forgehax.api.mapper.FieldMapping;
import dev.fiki.forgehax.main.util.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import dev.fiki.forgehax.main.util.reflection.types.ReflectionField;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static dev.fiki.forgehax.main.Common.getLocalPlayer;

@RegisterMod(
    name = "HorseJump",
    description = "Always max horse jump",
    category = Category.PLAYER
)
@RequiredArgsConstructor
public class HorseJump extends ToggleMod {
  @FieldMapping(parentClass = ClientPlayerEntity.class, value = "horseJumpPower")
  public final ReflectionField<Float> ClientPlayerEntity_horseJumpPower;

  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    ClientPlayerEntity_horseJumpPower.set(getLocalPlayer(), 1.F);
  }
}
