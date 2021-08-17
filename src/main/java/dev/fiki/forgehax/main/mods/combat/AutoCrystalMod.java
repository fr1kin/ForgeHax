package dev.fiki.forgehax.main.mods.combat;

import dev.fiki.forgehax.api.SimpleTimer;
import dev.fiki.forgehax.api.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.api.cmd.settings.FloatSetting;
import dev.fiki.forgehax.api.cmd.settings.IntegerSetting;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.entity.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.network.play.client.CUseEntityPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;

import java.util.function.Predicate;

import static dev.fiki.forgehax.main.Common.*;

/**
 * Created on 3/12/2018 by exkerbinator
 */
@RegisterMod(
    name = "AutoCrystal",
    description = "Automatically detonates nearby end crystals",
    category = Category.COMBAT
)
public class AutoCrystalMod extends ToggleMod {

  public final FloatSetting maxDistance = newFloatSetting()
      .name("max-distance")
      .description("maximum distance to detonate crystals")
      .defaultTo(3f)
      .min(0f)
      .build();

  public final FloatSetting minDistance = newFloatSetting()
      .name("min-distance")
      .description("minimum distance to detonate crystals")
      .defaultTo(0f)
      .min(0f)
      .build();

  public final FloatSetting minHeight = newFloatSetting()
      .name("min-height")
      .description("detonate crystals with a relative y coord greater than this value")
      .defaultTo(-5f)
      .build();

  public final IntegerSetting delay = newIntegerSetting()
      .name("delay")
      .description("delay between detonations in ms")
      .defaultTo(10)
      .min(0)
      .build();

  public final BooleanSetting checkEnemy = newBooleanSetting()
      .name("check-enemy")
      .description("only detonate crystals close to enemy players")
      .defaultTo(true)
      .build();

  public final FloatSetting maxEnemyDistance = newFloatSetting()
      .name("max-enemy-distance")
      .description("maximum distance from crystal to enemy")
      .defaultTo(10f)
      .min(0f)
      .build();

  private final SimpleTimer timer = new SimpleTimer();

  @Override
  public void onEnabled() {
    timer.start();
  }

  private Predicate<Entity> playerWithinDistance(float dist) {
    return k -> getLocalPlayer().distanceToSqr(k) < dist * dist;
  }

  private boolean enemyWithinDistance(Entity e, float dist) {
    Vector3d delta = new Vector3d(dist, dist, dist);
    AxisAlignedBB bb = new AxisAlignedBB(e.position().subtract(delta), e.position().add(delta));
    return getWorld().getEntitiesOfClass(PlayerEntity.class, bb).stream()
        .filter(p -> !p.is(getLocalPlayer()))
        .anyMatch(p -> e.distanceToSqr(p) < dist * dist);
  }

  @SubscribeListener
  public void onTick(LocalPlayerUpdateEvent event) {
    if (getLocalPlayer() != null) {
      // Short-circuit if the timer check will fail
      if (!timer.hasTimeElapsed(delay.getValue())) {
        return;
      }

      Vector3d delta = new Vector3d(maxDistance.getValue(), maxDistance.getValue(), maxDistance.getValue());
      AxisAlignedBB bb =
          new AxisAlignedBB(
              getLocalPlayer().position().subtract(delta),
              getLocalPlayer().position().add(delta));
      getWorld()
          .getEntitiesOfClass(EnderCrystalEntity.class, bb).stream()
          // Re-check timer, since it may have been reset in a previous iteration
          .filter(__ -> timer.hasTimeElapsed(delay.getValue()))
          .filter(e -> e.getY() - getLocalPlayer().getY() >= minHeight.getValue())
          .filter(playerWithinDistance(maxDistance.getValue()))
          .filter(playerWithinDistance(minDistance.getValue()).negate())
          .filter(e -> !checkEnemy.getValue() || enemyWithinDistance(e, maxEnemyDistance.getValue()))
          .forEach(e -> {
            sendNetworkPacket(new CUseEntityPacket(e, e.isShiftKeyDown()));
            sendNetworkPacket(new CAnimateHandPacket(Hand.MAIN_HAND));
            timer.start();
          });
    }
  }
}
