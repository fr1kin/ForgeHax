package dev.fiki.forgehax.main.mods.misc;

import dev.fiki.forgehax.api.cmd.settings.StringSetting;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.ForgeHaxEvent;
import dev.fiki.forgehax.api.events.entity.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.api.events.world.WorldUnloadEvent;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.main.Common;
import net.minecraft.util.math.BlockPos;

@RegisterMod(
    name = "BaritoneCompatibility",
    description = "the lazy compatibility mod",
    category = Category.MISC
)
public class BaritoneCompatibility extends ToggleMod {

  private final StringSetting on_string = newStringSetting()
      .name("on-string")
      .description("Message to enable baritone")
      .defaultTo("#mine diamond_ore")
      .build();

  private final StringSetting off_string = newStringSetting()
      .name("off-string")
      .description("Message to disable baritone")
      .defaultTo("#stop")
      .build();

  private boolean off = false;
  private boolean once = false;

  private void turnOn() {
    off = false;
    Common.getLocalPlayer().sendChatMessage(on_string.getValue());
  }

  private void turnOff() {
    off = true;
    Common.getLocalPlayer().sendChatMessage(off_string.getValue());
  }

  @Override
  protected void onDisabled() {
    off = once = false;
  }

  @SubscribeListener
  public void onWorldUnload(WorldUnloadEvent event) {
    onDisabled();
  }

  @SubscribeListener
  public void onTick(LocalPlayerUpdateEvent event) {
    if (!once) {
      once = true;
      BlockPos pos = Common.getLocalPlayer().getPosition();
      if (pos.getX() != 0 && pos.getZ() != 0) {
        turnOn();
      }
    }
  }

  @SubscribeListener
  public void onEvent(ForgeHaxEvent event) {
    if (Common.getLocalPlayer() == null) {
      return;
    }

    switch (event.getType()) {
      case EATING_START:
      case EATING_SELECT_FOOD: {
        if (!off) {
          turnOff();
        }
        break;
      }
      case EATING_STOP: {
        turnOn();
        break;
      }
    }
  }
}
