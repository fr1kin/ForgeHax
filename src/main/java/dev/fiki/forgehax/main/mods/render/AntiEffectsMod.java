package dev.fiki.forgehax.main.mods.render;

import com.google.common.collect.Sets;
import dev.fiki.forgehax.api.cmd.argument.Arguments;
import dev.fiki.forgehax.api.cmd.flag.EnumFlag;
import dev.fiki.forgehax.api.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.api.cmd.settings.collections.SimpleSettingSet;
import dev.fiki.forgehax.api.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.api.mapper.MethodMapping;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.api.reflection.types.ReflectionMethod;
import lombok.RequiredArgsConstructor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.Effects;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod(
    name = "AntiPotionEffects",
    description = "Removes potion effects",
    category = Category.RENDER
)
@RequiredArgsConstructor
public class AntiEffectsMod extends ToggleMod {
  @MethodMapping(parentClass = LivingEntity.class, value = "resetPotionEffectMetadata")
  private final ReflectionMethod<Void> LivingEntity_resetPotionEffectMetadata;

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
      .defaultsTo(Effects.NAUSEA)
      .defaultsTo(Effects.INVISIBILITY)
      .defaultsTo(Effects.BLINDNESS)
      .defaultsTo(Effects.WITHER)
      .build();

  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    effects.forEach(event.getEntityLiving()::removeActivePotionEffect);

    // removes particle effect
    LivingEntity_resetPotionEffectMetadata.invoke(event.getEntityLiving());
  }

  @SubscribeEvent
  public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
    if (noParticles.getValue()) {
      LivingEntity_resetPotionEffectMetadata.invoke(event.getEntityLiving());
    }
  }
}
