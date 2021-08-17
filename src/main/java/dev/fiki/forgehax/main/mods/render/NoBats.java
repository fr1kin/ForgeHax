package dev.fiki.forgehax.main.mods.render;

import dev.fiki.forgehax.api.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.game.EntitySoundEvent;
import dev.fiki.forgehax.api.events.render.LivingRenderEvent;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.util.SoundEvents;

@RegisterMod(
    name = "NoBats",
    description = "Will mute bat noises and optionally not render them",
    category = Category.RENDER
)
public class NoBats extends ToggleMod {
  private final BooleanSetting invisible = newBooleanSetting()
      .name("invisible")
      .description("Make bats invisible to the player")
      .defaultTo(false)
      .build();

  @SubscribeListener
  public void onRenderLiving(LivingRenderEvent.Pre<?, ?> event) {
    if (invisible.isEnabled() && event.getLiving() instanceof BatEntity) {
      event.setCanceled(true);
    }
  }

  @SubscribeListener
  public void onPlaySound(EntitySoundEvent event) {
    if (event.getSound().equals(SoundEvents.BAT_AMBIENT)
        || event.getSound().equals(SoundEvents.BAT_DEATH)
        || event.getSound().equals(SoundEvents.BAT_HURT)
        || event.getSound().equals(SoundEvents.BAT_LOOP)
        || event.getSound().equals(SoundEvents.BAT_TAKEOFF)) {
      event.setVolume(0.f);
      event.setPitch(0.f);
      event.setCanceled(true);
    }
  }
}
