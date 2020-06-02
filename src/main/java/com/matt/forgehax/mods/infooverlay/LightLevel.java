package com.matt.forgehax.mods.infooverlay;

import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.BlockPos;

import static com.matt.forgehax.Helper.getLocalPlayer;

@RegisterMod
public class LightLevel extends ToggleMod {

  public LightLevel() {
    super(Category.GUI, "LightLevel", false,
      "Shows the light level of the block you're currently standing on");
  }

  @Override
  public boolean isInfoDisplayElement() {
    return true;
  }

  @Override
  public boolean notInList() {
	return true;
  }

  public String getInfoDisplayText() {
    EntityPlayerSP player = getLocalPlayer();
    double thisX = player.posX;
    double thisY = player.posY;
    double thisZ = player.posZ;

    BlockPos position = new BlockPos(thisX, thisY ,thisZ);

    return "Light: " + String.format("%s", MC.world.getLight(position));
  }
}
