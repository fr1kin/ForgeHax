package com.matt.forgehax.mods;

import com.google.common.collect.Lists;
import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collections;

public class AimbotMod extends ToggleMod {
    //private Set<Entity> entitySet = Collections.newSetFromMap(Maps.<Entity, Boolean>newConcurrentMap());

    public Property silent;
    public Property autoAttack;
    public Property holdTarget;

    public Property fov;
    public Property range;
    public Property cooldownPercent;

    public Property players;
    public Property hostileMobs;
    public Property friendlyMobs;

    public Property drawFOV;

    private Angle aimViewAngles = new Angle(0, 0, 0);
    private boolean aiming = false;

    public AimbotMod(String modName, boolean defaultValue, String description, int key) {
        super(modName, defaultValue, description, key);
    }

    public boolean canAttack(EntityPlayer localPlayer, Entity target) {
        return localPlayer.getCooledAttackStrength(0.f) >= (cooldownPercent.getDouble() / 100.f) &&
                (autoAttack.getBoolean() || Bindings.attack.getBinding().isKeyDown()); // need to work on this

    }

    /**
     * Check if the entity is a valid target to acquire
     */
    public boolean isValidTarget(Entity entity, Vec3d entPos, Vec3d selfPos, Vec3d lookVec, Angle viewAngles) {
        return EntityUtils.isLiving(entity) &&
                EntityUtils.isAlive(entity) &&
                !entity.equals(MC.thePlayer) &&
                EntityUtils.isValidEntity(entity) &&
                MC.thePlayer.canEntityBeSeen(entity) && (
                (EntityUtils.isPlayer(entity) && players.getBoolean()) ||
                        (EntityUtils.isHostileMob(entity) && hostileMobs.getBoolean()) ||
                        (EntityUtils.isFriendlyMob(entity) && friendlyMobs.getBoolean())
                ) &&
                isInRange(entPos, selfPos) &&
                isInFOVRange(viewAngles, entPos.subtract(selfPos));
    }

    /**
     * Check if entity is in attack range
     */
    public boolean isInRange(Vec3d fromPos, Vec3d toPos) {
        return fromPos.distanceTo(toPos) <= range.getDouble();
    }

    public boolean isInFOVRange(Angle selfAngle, Vec3d diffPos) {
        double value = fov.getDouble();
        if(value >= 180) {
            return true;
        } else {
            Angle diff = VectorUtils.vectorAngle(diffPos);
            double pitch = Math.abs(Utils.normalizeAngle(selfAngle.p - diff.p));
            double yaw = Math.abs(Utils.normalizeAngle(selfAngle.y - diff.y));
            return pitch <= value && yaw <= value;
        }
    }

    /**
     * Finds entity closest to crosshair
     */
    public Entity findTargetEntity(Vec3d selfPos, Vec3d selfLookVec, Angle viewAngles) {
        final World world = Minecraft.getMinecraft().theWorld;
        final Vec3d selfLookVecNormal = selfLookVec.normalize();
        Entity target = null;
        double shortestDistance = -1;
        synchronized (world.loadedEntityList) {
            for (Entity entity : Collections.synchronizedList(Lists.newArrayList(world.loadedEntityList))) {
                if(entity != null) {
                    Vec3d pos = EntityUtils.getOBBCenter(entity);
                    if (isValidTarget(entity, pos, selfPos, selfLookVec, viewAngles)) {
                        double distance = pos
                                .subtract(selfPos)
                                .normalize()
                                .subtract(selfLookVecNormal)
                                .lengthVector();
                        if (shortestDistance == -1 || distance < shortestDistance) {
                            target = entity;
                            shortestDistance = distance;
                        }
                    }
                }
            }
        }
        PlayerUtils.setTargetEntity(target);
        return target;
    }

    public Angle calculateAngles(Entity entity) {
        Vec3d selfPos = EntityUtils.getEyePos(MC.thePlayer);
        Vec3d targetPos = EntityUtils.getOBBCenter(entity);
        // convert vector to angle
        Angle aimAngle = VectorUtils.vectorAngle(targetPos.subtract(selfPos));
        return aimAngle.normalize();
    }

    @Override
    public void loadConfig(Configuration configuration) {
        addSettings(
                silent = configuration.get(getModName(),
                        "silent",
                        true,
                        "Won't lock onto target"
                ),
                autoAttack = configuration.get(getModName(),
                        "auto attack",
                        true,
                        "Automatically attack"
                ),
                holdTarget = configuration.get(getModName(),
                        "hold target",
                        false,
                        "Keeps target until it is no longer a valid attack target"
                ),
                fov = configuration.get(getModName(),
                        "fov",
                        40.D,
                        "Aimbot field of view",
                        0.D,
                        180.D
                ),
                range = configuration.get(getModName(),
                        "range",
                        4.5D,
                        "Attack range"
                ),
                cooldownPercent = configuration.get(getModName(),
                        "cooldown percent",
                        100.D,
                        "What cooldown % to attack again at",
                        0.D,
                        100.D
                ),
                players = configuration.get(getModName(),
                        "players",
                        true,
                        "Attack players"
                ),
                hostileMobs = configuration.get(getModName(),
                        "hostile mobs",
                        true,
                        "Attack hostile mobs"
                ),
                friendlyMobs = configuration.get(getModName(),
                        "friendly mobs",
                        true,
                        "Attack friendly mobs"
                ),
                drawFOV = configuration.get(getModName(),
                        "draw FOV",
                        false,
                        "Draws field of view on screen (dont use atm)"
                )
        );
    }

    @Override
    public void onDisabled() {
        PlayerUtils.setTargetEntity(null);
    }

    @SubscribeEvent
    public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
        EntityPlayer localPlayer = MC.thePlayer;
        Entity target = PlayerUtils.getTargetEntity();
        Vec3d selfPos = EntityUtils.getEyePos(localPlayer);
        Vec3d selfLookVec = localPlayer.getLookVec();
        Angle viewAngles = VectorUtils.vectorAngle(selfLookVec);
        // disable aiming from last tick
        aiming = false;
        aimViewAngles = VectorUtils.vectorAngle(selfLookVec);
        if(holdTarget.getBoolean()) {
            if(target == null ||
                    !isValidTarget(target, EntityUtils.getOBBCenter(target), selfPos, selfLookVec, viewAngles))
                target = findTargetEntity(selfPos, selfLookVec, viewAngles);
        } else target = findTargetEntity(selfPos, selfLookVec, viewAngles);
        if(target != null) {
            Angle aim = calculateAngles(target);
            if(!silent.getBoolean())
                PlayerUtils.setViewAngles(aim);
            if (canAttack(localPlayer, target)) {
                // attack entity
                MC.playerController.attackEntity(MC.thePlayer, target);
                // swing hand
                localPlayer.swingArm(EnumHand.MAIN_HAND);
                // for rotation packets
                aiming = true;
                aimViewAngles = aim;
            }
        }
    }

    @SubscribeEvent
    public void onPacketSending(PacketEvent.SendEvent.Pre event) {
        if(event.getPacket() instanceof CPacketPlayer) {
            CPacketPlayer packet = (CPacketPlayer)event.getPacket();
            if(packet.rotating &&
                    aiming &&
                    aimViewAngles != null) {
                packet.pitch = (float) aimViewAngles.p;
                packet.yaw = (float) aimViewAngles.y;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDrawScreen(RenderGameOverlayEvent.Text event) {
        if(drawFOV.getBoolean()) {
            // shamelessly stolen from a garrys mod cheat :)
            // thx RabidToaster
            Angle view = new Angle(VectorUtils.vectorAngle(MC.thePlayer.getLookVec().normalize()));
            view.p += fov.getDouble();
            VectorUtils.ScreenPos screen = VectorUtils.toScreen(
                    EntityUtils.getEyePos(MC.thePlayer).add(view.forward().scale(100)));
            double midX = event.getResolution().getScaledWidth() / 2;
            double midY = event.getResolution().getScaledHeight() / 2;
            double length = Math.abs(midY - screen.y);
            for(int x = -1; x <= 1; x++ ) {
                for(int y = -1; y <= 1; y++ ) {
                    if(x != 0 || y != 0) {
                        Vec3d normal = new Vec3d(x, y, 0).normalize().scale(length);
                        RenderUtils.drawRect((int) (midX + normal.xCoord) - 2, (int) (midY + normal.yCoord) - 2, 5, 5, Utils.Colors.BLACK);
                        RenderUtils.drawRect((int) (midX + normal.xCoord) - 1, (int) (midY + normal.yCoord) - 1, 3, 3, Utils.Colors.WHITE);
                    }
                }
            }
        }
    }
}
