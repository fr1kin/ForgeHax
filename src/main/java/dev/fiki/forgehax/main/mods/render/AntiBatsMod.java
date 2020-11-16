package dev.fiki.forgehax.main.mods.render;

import dev.fiki.forgehax.main.util.entity.EntityUtils;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod(
    name = "AntiBats",
    description = "666 KILL BATS 666",
    category = Category.RENDER
)
public class AntiBatsMod extends ToggleMod {
  @Override
  public void onEnabled() {
    EntityUtils.setBatsDisabled(true);
  }

  @Override
  public void onDisabled() {
    EntityUtils.setBatsDisabled(false);
  }

  @SubscribeEvent
  public void onRenderLiving(RenderLivingEvent.Pre<? extends LivingEntity, ? extends EntityModel<?>> event) {
    if (event.getEntity() instanceof BatEntity) {
      event.setCanceled(true);
    }
  }

  @SubscribeEvent
  public void onPlaySound(PlaySoundAtEntityEvent event) {
    if (event.getSound().equals(SoundEvents.ENTITY_BAT_AMBIENT)
        || event.getSound().equals(SoundEvents.ENTITY_BAT_DEATH)
        || event.getSound().equals(SoundEvents.ENTITY_BAT_HURT)
        || event.getSound().equals(SoundEvents.ENTITY_BAT_LOOP)
        || event.getSound().equals(SoundEvents.ENTITY_BAT_TAKEOFF)) {
      event.setVolume(0.f);
      event.setPitch(0.f);
      event.setCanceled(true);
    }
  }
}
