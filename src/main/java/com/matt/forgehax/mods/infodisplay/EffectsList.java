package com.matt.forgehax.mods.infodisplay;

import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * EffectsList mod
 * by OverFloyd, june 2020
 */
@RegisterMod
public class EffectsList extends ToggleMod {

  private final Setting<Boolean> hideIcons =
    getCommandStub()
      .builders()
      .<Boolean>newSettingBuilder()
      .name("hide-icons")
      .description("Will hide the default Minecraft icons.")
      .defaultTo(true)
      .build();

  public final Setting<Boolean> showList =
    getCommandStub()
      .builders()
      .<Boolean>newSettingBuilder()
      .name("show-list")
      .description("Shows epic list of effects.")
      .defaultTo(true)
      .build();

  public EffectsList() {
    super(Category.GUI,"EffectsList", true, "Shows the effects you're currently affected by");
  }

  @Override
  public boolean isInfoDisplayElement() {
    return false;
  }

  @SubscribeEvent
  public void onRenderTick(RenderGameOverlayEvent.Pre event) {
    if (hideIcons.get() && event.getType() == RenderGameOverlayEvent.ElementType.POTION_ICONS) {
      event.setCanceled(true);
    }
  }
}
