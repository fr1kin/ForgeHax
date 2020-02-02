package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.command.Setting;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.util.SimpleTimer;

import java.util.function.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.network.play.client.CUseEntityPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Created on 3/12/2018 by exkerbinator
 */
@RegisterMod
public class AutoCrystalMod extends ToggleMod {
  
  public final Setting<Float> maxDistance =
      getCommandStub()
          .builders()
          .<Float>newSettingBuilder()
          .name("max-distance")
          .description("maximum distance to detonate crystals")
          .defaultTo(3f)
          .min(0f)
          .build();
  
  public final Setting<Float> minDistance =
      getCommandStub()
          .builders()
          .<Float>newSettingBuilder()
          .name("min-distance")
          .description("minimum distance to detonate crystals")
          .defaultTo(0f)
          .min(0f)
          .build();
  
  public final Setting<Float> minHeight =
      getCommandStub()
          .builders()
          .<Float>newSettingBuilder()
          .name("min-height")
          .description("detonate crystals with a relative y coord greater than this value")
          .defaultTo(-5f)
          .build();
  
  public final Setting<Integer> delay =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("delay")
          .description("delay between detonations in ms")
          .defaultTo(10)
          .min(0)
          .build();
  
  public final Setting<Boolean> checkEnemy =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("check-enemy")
          .description("only detonate crystals close to enemy players")
          .defaultTo(true)
          .build();
  
  public final Setting<Float> maxEnemyDistance =
      getCommandStub()
          .builders()
          .<Float>newSettingBuilder()
          .name("max-enemy-distance")
          .description("maximum distance from crystal to enemy")
          .defaultTo(10f)
          .min(0f)
          .build();
  
  public AutoCrystalMod() {
    super(Category.COMBAT, "AutoCrystal", false, "Automatically detonates nearby end crystals");
  }
  
  private SimpleTimer timer = new SimpleTimer();
  
  @Override
  public void onEnabled() {
    timer.start();
  }
  
  private Predicate<Entity> playerWithinDistance(float dist) {
    return k -> Common.getLocalPlayer().getDistanceSq(k) < dist * dist;
  }
  
  private boolean enemyWithinDistance(Entity e, float dist) {
    Vec3d delta = new Vec3d(dist, dist, dist);
    AxisAlignedBB bb =
        new AxisAlignedBB(e.getPositionVector().subtract(delta), e.getPositionVector().add(delta));
    return Common.getWorld().getEntitiesWithinAABB(PlayerEntity.class, bb).stream()
        .filter(p -> !p.isEntityEqual(Common.getLocalPlayer()))
        .anyMatch(p -> e.getDistanceSq(p) < dist * dist);
  }
  
  @SubscribeEvent
  public void onTick(LocalPlayerUpdateEvent event) {
    if (Common.getWorld() != null && Common.getLocalPlayer() != null) {
      // Short-circuit if the timer check will fail
      if (!timer.hasTimeElapsed(delay.get())) {
        return;
      }
      
      Vec3d delta = new Vec3d(maxDistance.get(), maxDistance.get(), maxDistance.get());
      AxisAlignedBB bb =
          new AxisAlignedBB(
              Common.getLocalPlayer().getPositionVector().subtract(delta),
              Common.getLocalPlayer().getPositionVector().add(delta));
      Common.getWorld()
          .getEntitiesWithinAABB(EnderCrystalEntity.class, bb).stream()
          // Re-check timer, since it may have been reset in a previous iteration
          .filter(__ -> timer.hasTimeElapsed(delay.get()))
          .filter(
              e ->
                  e.getPosition().getY() - Common.getLocalPlayer().getPosition().getY() >= minHeight.get())
          .filter(playerWithinDistance(maxDistance.get()))
          .filter(playerWithinDistance(minDistance.get()).negate())
          .filter(e -> !checkEnemy.get() || enemyWithinDistance(e, maxEnemyDistance.get()))
          .forEach(
              e -> {
                Common.sendNetworkPacket(new CUseEntityPacket(e));
                Common.sendNetworkPacket(new CAnimateHandPacket(Hand.MAIN_HAND));
                timer.start();
              });
    }
  }
}
