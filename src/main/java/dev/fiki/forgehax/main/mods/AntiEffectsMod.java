package dev.fiki.forgehax.main.mods;

import com.google.common.collect.Sets;
import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.cmd.argument.Arguments;
import dev.fiki.forgehax.main.util.cmd.flag.EnumFlag;
import dev.fiki.forgehax.main.util.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.main.util.cmd.settings.collections.SimpleSettingSet;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.util.reflection.FastReflection;
import net.minecraft.potion.Effect;
import net.minecraft.potion.Effects;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod
public class AntiEffectsMod extends ToggleMod {

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

  public AntiEffectsMod() {
    super(Category.RENDER, "AntiPotionEffects", false, "Removes potion effects");
  }

  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    effects.forEach(event.getEntityLiving()::removeActivePotionEffect);

    // removes particle effect
    FastReflection.Methods.LivingEntity_resetPotionEffectMetadata.invoke(event.getEntityLiving());
  }

  @SubscribeEvent
  public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
    if (noParticles.getValue()) {
      FastReflection.Methods.LivingEntity_resetPotionEffectMetadata.invoke(event.getEntityLiving());
    }
  }
}
