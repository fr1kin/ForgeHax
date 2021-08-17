package dev.fiki.forgehax.main.mods.world;

import dev.fiki.forgehax.api.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.game.PreGameTickEvent;
import dev.fiki.forgehax.api.events.world.WorldChangeEvent;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.asm.events.packet.PacketInboundEvent;
import net.minecraft.network.play.server.SChangeGameStatePacket;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import static dev.fiki.forgehax.main.Common.*;

@RegisterMod(
    name = "NoWeather",
    description = "Disables weather",
    category = Category.WORLD
)
public class NoWeather extends ToggleMod {

  private boolean isRaining = false;
  private float rainStrength = 0.f;
  private float previousRainStrength = 0.f;

  private final BooleanSetting showStatus = newBooleanSetting()
      .name("hud-status")
      .description("show info about suppressed weather")
      .defaultTo(true)
      .build();

  private void saveState(World world) {
    if (world != null) {
      setState(world.getLevelData().isRaining(), world.rainLevel, world.oRainLevel);
    } else {
      setState(false, 1.f, 1.f);
    }
  }

  private void setState(boolean raining, float rainStrength, float previousRainStrength) {
    this.isRaining = raining;
    setState(rainStrength, previousRainStrength);
  }

  private void setState(float rainStrength, float previousRainStrength) {
    this.rainStrength = rainStrength;
    this.previousRainStrength = previousRainStrength;
  }

  private void disableRain() {
    if (getWorld() != null) {
      getWorld().getLevelData().setRaining(false);
      getWorld().setRainLevel(0.f);
    }
  }

  public void resetState() {
    if (getWorld() != null) {
      getWorld().getLevelData().setRaining(isRaining);
      getWorld().rainLevel = rainStrength;
      getWorld().oRainLevel = previousRainStrength;
    }
  }

  @Override
  public void onEnabled() {
    saveState(getWorld());
  }

  @Override
  public void onDisabled() {
    resetState();
  }

  @SubscribeListener
  public void onWorldChange(WorldChangeEvent event) {
    saveState(event.getWorld());
  }

  @SubscribeListener
  public void onWorldTick(PreGameTickEvent event) {
    disableRain();
  }

  @SubscribeListener
  public void onPacketIncoming(PacketInboundEvent event) {
    if (event.getPacket() instanceof SChangeGameStatePacket) {
      SChangeGameStatePacket.State state = ((SChangeGameStatePacket) event.getPacket()).getEvent();
      float strength = ((SChangeGameStatePacket) event.getPacket()).getParam();
      boolean isRainState = false;
      if (state == SChangeGameStatePacket.STOP_RAINING) {
        isRainState = false;
        setState(false, 0.f, 0.f);
      } else if (state == SChangeGameStatePacket.START_RAINING) {
        // start rain
        isRainState = true;
        setState(true, 1.f, 1.f);
      } else if (state == SChangeGameStatePacket.THUNDER_LEVEL_CHANGE) {
        // fade value: sky brightness
        isRainState = true; // needs to be cancelled to avoid flicker
      }
      if (isRainState) {
        disableRain();
        event.setCanceled(true);
      }
    }
  }

  @Override
  public String getDisplayText() {
    if (isRaining
        && showStatus.getValue()
        && isInWorld()) {
      Biome biome = getWorld().getBiome(getLocalPlayer().blockPosition());
      boolean canRain = Biome.RainType.RAIN.equals(biome.getPrecipitation());
      boolean canSnow = Biome.RainType.SNOW.equals(biome.getPrecipitation());

      String status;

      if (getWorld().isThundering()) {
        status = "[Thunder]";
      } else if (canSnow) {
        status = "[Snowing]";
      } else if (!canRain) {
        status = "[Cloudy]";
      } else {
        status = "[Raining]";
      }

      return super.getDisplayText() + status;
    } else {
      return super.getDisplayText();
    }
  }
}
