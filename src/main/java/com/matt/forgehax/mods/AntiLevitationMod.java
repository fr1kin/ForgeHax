package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.potion.Effect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

/** Created on 11/28/2016 by fr1kin */
@RegisterMod
public class AntiLevitationMod extends ToggleMod {
  public AntiLevitationMod() {
    super(Category.PLAYER, "AntiLevitation", false, "No levitation");
  }

  private static final Effect LEVITATION = Objects.requireNonNull(ForgeRegistries.POTIONS.getValue(new ResourceLocation("levitation")));


  @SubscribeEvent
  public void onUpdate(LocalPlayerUpdateEvent event) {
    if (getLocalPlayer().isPotionActive(LEVITATION)) {
      getLocalPlayer().removeActivePotionEffect(LEVITATION);
    }
  }
}
