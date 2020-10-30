package dev.fiki.forgehax.main.util.typeconverter.registry;

import net.minecraft.potion.Effect;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

public class EffectType extends AbstractRegistryType<Effect> {
  @Override
  public IForgeRegistry<Effect> getRegistry() {
    return ForgeRegistries.POTIONS;
  }
}
