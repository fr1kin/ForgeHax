package com.matt.forgehax.mods.infodisplay;

import com.matt.forgehax.util.mod.EntityCoords;
import com.matt.forgehax.util.mod.loader.RegisterMod;

@RegisterMod
public class Biome extends EntityCoords {

  public Biome() {
    super("Biome", "Shows the biome you're currently in");
  }

  @Override
  public boolean isInfoDisplayElement() {
    return true;
  }

  public String getInfoDisplayText() {
    return "Biome: " + MC.world.getBiomeForCoordsBody(getPosition()).getBiomeName();
  }
}
