package dev.fiki.forgehax.main.mods.misc;

import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.cmd.settings.StringSetting;
import dev.fiki.forgehax.main.util.events.ForgeHaxEvent;
import dev.fiki.forgehax.main.util.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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

  @SubscribeEvent
  public void onWorldUnload(WorldEvent.Unload event) {
    onDisabled();
  }

  @SubscribeEvent
  public void onTick(LocalPlayerUpdateEvent event) {
    if (!once) {
      once = true;
      BlockPos pos = Common.getLocalPlayer().getPosition();
      if (pos.getX() != 0 && pos.getZ() != 0) {
        turnOn();
      }
    }
  }

  @SubscribeEvent
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
