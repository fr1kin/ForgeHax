package dev.fiki.forgehax.main.mods.render;

import com.google.common.collect.Sets;
import dev.fiki.forgehax.api.asm.MapMethod;
import dev.fiki.forgehax.api.cmd.argument.Arguments;
import dev.fiki.forgehax.api.cmd.flag.EnumFlag;
import dev.fiki.forgehax.api.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.api.cmd.settings.collections.SimpleSettingSet;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.entity.LivingUpdateEvent;
import dev.fiki.forgehax.api.events.entity.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.api.reflection.types.ReflectionMethod;
import lombok.RequiredArgsConstructor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.Effects;

@RegisterMod(
    name = "AntiPotionEffects",
    description = "Removes potion effects",
    category = Category.RENDER
)
@RequiredArgsConstructor
public class AntiEffectsMod extends ToggleMod {
  @MapMethod(parentClass = LivingEntity.class, value = "removeEffectParticles")
  private final ReflectionMethod<Void> LivingEntity_removeEffectParticles;

  private final BooleanSetting noParticles = newBooleanSetting()
      .name("no-particles")
      .description("Stops the particle effect from rendering on other entities")
      .defaultTo(false)
      .build();

  private final SimpleSettingSet<Effect> effects = newSimpleSettingSet(Effect.class)
      .name("effects")
      .description("List of potion effects to remove from the player")
      .flag(EnumFlag.EXECUTOR_MAIN_THREAD)
      .argument(Arguments.newEffectArgument()
          .label("name")
          .build())
      .supplier(Sets::newHashSet)
      .defaultsTo(Effects.CONFUSION)
      .defaultsTo(Effects.INVISIBILITY)
      .defaultsTo(Effects.BLINDNESS)
      .defaultsTo(Effects.WITHER)
      .build();

  @SubscribeListener
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    effects.forEach(event.getPlayer()::removeEffect);

    // removes particle effect
    LivingEntity_removeEffectParticles.invoke(event.getPlayer());
  }

  @SubscribeListener
  public void onLivingUpdate(LivingUpdateEvent event) {
    if (noParticles.getValue()) {
      LivingEntity_removeEffectParticles.invoke(event.getLiving());
    }
  }
}
