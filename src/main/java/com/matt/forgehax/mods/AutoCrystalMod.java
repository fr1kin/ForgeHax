package com.matt.forgehax.mods;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.SimpleTimer;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.function.Predicate;

import static com.matt.forgehax.Helper.*;

/**
 * Created on 3/12/2018 by exkerbinator
 */
@RegisterMod
public class AutoCrystalMod extends ToggleMod {
    public final Setting<Float> maxDistance = getCommandStub().builders().<Float>newSettingBuilder()
            .name("maxDistance")
            .description("maximum distance to detonate crystals")
            .defaultTo(3f)
            .min(0f)
            .build();

    public final Setting<Float> minDistance = getCommandStub().builders().<Float>newSettingBuilder()
            .name("minDistance")
            .description("minimum distance to detonate crystals")
            .defaultTo(0f)
            .min(0f)
            .build();

    public final Setting<Float> minHeight = getCommandStub().builders().<Float>newSettingBuilder()
            .name("minHeight")
            .description("detonate crystals with a relative y coord greater than this value")
            .defaultTo(-5f)
            .build();

    public final Setting<Integer> delay = getCommandStub().builders().<Integer>newSettingBuilder()
            .name("delay")
            .description("delay between detonations in ms")
            .defaultTo(10)
            .min(0)
            .build();

    public final Setting<Boolean> checkEnemy = getCommandStub().builders().<Boolean>newSettingBuilder()
            .name("checkEnemy")
            .description("only detonate crystals close to enemy players")
            .defaultTo(true)
            .build();

    public final Setting<Float> maxEnemyDistance = getCommandStub().builders().<Float>newSettingBuilder()
            .name("maxEnemyDistance")
            .description("maximum distance from crystal to enemy")
            .defaultTo(10f)
            .min(0f)
            .build();

    public AutoCrystalMod() {
        super(Category.PLAYER, "AutoCrystal", false, "Automatically detonates nearby end crystals");
    }

    private SimpleTimer timer = new SimpleTimer();

    @Override
    public void onEnabled() {
        timer.start();
    }

    private Predicate<Entity> playerWithinDistance(float dist) {
        return k -> getLocalPlayer().getDistanceSqToEntity(k) < dist * dist;
    }

    private boolean enemyWithinDistance(Entity e, float dist) {
        Vec3d delta = new Vec3d(dist, dist, dist);
        AxisAlignedBB bb = new AxisAlignedBB(e.getPositionVector().subtract(delta), e.getPositionVector().add(delta));
        return getWorld().getEntitiesWithinAABB(EntityPlayer.class, bb).stream()
                .filter(p -> !p.isEntityEqual(getLocalPlayer()))
                .anyMatch(p -> e.getDistanceSqToEntity(p) < dist * dist);
    }

    @SubscribeEvent
    public void onTick(LocalPlayerUpdateEvent event) {
        if (getWorld() != null && getLocalPlayer() != null) {
            // Short-circuit if the timer check will fail
            if (!timer.hasTimeElapsed(delay.get())) return;

            Vec3d delta = new Vec3d(maxDistance.get(), maxDistance.get(), maxDistance.get());
            AxisAlignedBB bb = new AxisAlignedBB(getLocalPlayer().getPositionVector().subtract(delta),
                    getLocalPlayer().getPositionVector().add(delta));
            getWorld().getEntitiesWithinAABB(EntityEnderCrystal.class, bb).stream()
                    // Re-check timer, since it may have been reset in a previous iteration
                    .filter(__ -> timer.hasTimeElapsed(delay.get()))
                    .filter(e -> e.getPosition().getY() - getLocalPlayer().getPosition().getY() >= minHeight.get())
                    .filter(playerWithinDistance(maxDistance.get()))
                    .filter(playerWithinDistance(minDistance.get()).negate())
                    .filter(e -> !checkEnemy.get() || enemyWithinDistance(e, maxEnemyDistance.get()))
                    .forEach(e -> {
                        getNetworkManager().sendPacket(new CPacketUseEntity(e));
                        getNetworkManager().sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
                        timer.start();
                    });
        }
    }
}
