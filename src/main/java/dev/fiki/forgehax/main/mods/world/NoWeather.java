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
      setState(world.getWorldInfo().isRaining(), world.rainingStrength, world.prevRainingStrength);
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
      getWorld().getWorldInfo().setRaining(false);
      getWorld().setRainStrength(0.f);
    }
  }

  public void resetState() {
    if (getWorld() != null) {
      getWorld().getWorldInfo().setRaining(isRaining);
      getWorld().rainingStrength = rainStrength;
      getWorld().prevRainingStrength = previousRainStrength;
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
      SChangeGameStatePacket.State state = ((SChangeGameStatePacket) event.getPacket()).func_241776_b_();
      float strength = ((SChangeGameStatePacket) event.getPacket()).getValue();
      boolean isRainState = false;
      if (state == SChangeGameStatePacket.field_241765_b_) {
        isRainState = false;
        setState(false, 0.f, 0.f);
      } else if (state == SChangeGameStatePacket.field_241766_c_) {
        // start rain
        isRainState = true;
        setState(true, 1.f, 1.f);
      } else if (state == SChangeGameStatePacket.field_241771_h_) {
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
      Biome biome = getWorld().getBiome(getLocalPlayer().getPosition());
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
