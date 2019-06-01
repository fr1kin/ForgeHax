package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;

import com.matt.forgehax.events.ForgeHaxEvent;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class BaritoneCompatibility extends ToggleMod {
  private final Setting<String> on_string =
      getCommandStub()
          .builders()
          .<String>newSettingBuilder()
          .name("on-string")
          .description("Message to enable baritone")
          .defaultTo("#mine diamond_ore")
          .build();

  private final Setting<String> off_string =
      getCommandStub()
          .builders()
          .<String>newSettingBuilder()
          .name("off-string")
          .description("Message to disable baritone")
          .defaultTo("#stop")
          .build();

  public BaritoneCompatibility() {
    super(Category.MISC, "BaritoneCompatibility", false, "the lazy compatibility mod");
  }

  private boolean off = false;
  private boolean once = false;

  private void turnOn() {
    off = false;
    getLocalPlayer().sendChatMessage(on_string.get());
  }

  private void turnOff() {
    off = true;
    getLocalPlayer().sendChatMessage(off_string.get());
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
    if(!once) {
      once = true;
      BlockPos pos = getLocalPlayer().getPosition();
      if(pos.getX() != 0 && pos.getZ() != 0) {
        turnOn();
      }
    }
  }

  @SubscribeEvent
  public void onEvent(ForgeHaxEvent event) {
    if(getLocalPlayer() == null)
      return;

    switch (event.getType()) {
      case EATING_START:
      case EATING_SELECT_FOOD: {
        if(!off) turnOff();
        break;
      }
      case EATING_STOP: {
        turnOn();
        break;
      }
    }
  }
}
