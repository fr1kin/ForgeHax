package com.matt.forgehax.util.mod;

import com.matt.forgehax.util.command.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;


public class EntityCoords extends ToggleMod {

  public final Setting<Boolean> viewEntity =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("view-entity")
          .description("Shows the current biome for viewentity (freecam)")
          .defaultTo(true)
          .build();

  public EntityCoords(String modName, String description) {
    super(Category.GUI, modName, false, description);
  }

  protected Entity getEntity() {
    return viewEntity.get() ? MC.getRenderViewEntity() : MC.player;
  }

  protected BlockPos getPosition() {
    return getEntity().getPosition();
  }
}
