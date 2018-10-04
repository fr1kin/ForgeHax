package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getWorld;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class FPSLock extends ToggleMod {
  private final Setting<Integer> fps =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("fps")
          .description("FPS to use when the world is loaded")
          .min(0)
          .defaultTo(60)
          .build();
  private final Setting<Integer> menu_fps =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("menu-fps")
          .description("FPS when the GUI is opened")
          .min(0)
          .defaultTo(60)
          .build();

  public FPSLock() {
    super(
        Category.MISC,
        "FPSLock",
        false,
        "Lock the fps to a lower-than-allowed value, and restore when disabled");
  }

  @SubscribeEvent
  void onTick(LocalPlayerUpdateEvent event) {
    if (getWorld() != null) MC.gameSettings.limitFramerate = fps.get();
    else MC.gameSettings.limitFramerate = menu_fps.get();
  }

  @SubscribeEvent
  void onWorldUnload(WorldEvent.Unload event) {
    MC.gameSettings.limitFramerate = menu_fps.get();
  }

  @Override
  protected void onDisabled() {
    MC.gameSettings.limitFramerate = menu_fps.get();
  }
}
