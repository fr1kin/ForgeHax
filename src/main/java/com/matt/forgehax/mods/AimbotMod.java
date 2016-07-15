package com.matt.forgehax.mods;

import com.google.common.collect.Lists;
import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collections;

public class AimbotMod extends ToggleMod {
    public Property silent;
    public Property autoAttack;
    public Property holdTarget;
    public Property projectileAimbot;
    public Property projectileTraceCheck;

    public Property fov;
    public Property range;
    public Property projectileRange;
    public Property cooldownPercent;

    public Property players;
    public Property hostileMobs;
    public Property friendlyMobs;

    public AimbotMod(String modName, boolean defaultValue, String description, int key) {
        super(modName, defaultValue, description, key);
    }

    public boolean canAttack(EntityPlayer localPlayer, Entity target) {
        return localPlayer.getCooledAttackStrength(0.f) >= (cooldownPercent.getDouble() / 100.f) &&
                (autoAttack.getBoolean() || Bindings.attack.getBinding().isKeyDown()); // need to work on this

    }

    public boolean isHoldingProjectileItem() {
        return ProjectileUtils.isThrowable(MC.thePlayer.getHeldItemMainhand());
    }

    public boolean canBowAimbot() {
        return projectileAimbot.getBoolean() &&
                isHoldingProjectileItem();
    }

    public boolean isVisible(Entity target) {
        if(canBowAimbot() && projectileTraceCheck.getBoolean()) {
            return ProjectileUtils.projectileTrajectoryHitsEntity(target, EntityUtils.getEyePos(getLocalPlayer()), getAimPos(target), null);
        } else return getLocalPlayer().canEntityBeSeen(target);
    }

    public Vec3d getAimPos(Entity entity) {
        /*
        Vec3d selfPos = getLocalPlayer().getPositionVector();
        Vec3d tarPos = entity.getPositionVector();
        Vec3d dir = tarPos.subtract(selfPos).normalize();
        return tarPos.add(new Vec3d(dir.xCoord, 0, dir.zCoord).scale(3));*/
        return EntityUtils.getInterpolatedPos(entity, 1).addVector(0, entity.getEyeHeight() / 2, 0);
    }

    /**
     * Check if the entity is a valid target to acquire
     */
    public boolean isValidTarget(Entity entity, Vec3d entPos, Vec3d selfPos, Vec3d lookVec, Angle viewAngles) {
        return EntityUtils.isLiving(entity) &&
                EntityUtils.isAlive(entity) &&
                !entity.equals(MC.thePlayer) &&
                EntityUtils.isValidEntity(entity) && (
                (EntityUtils.isPlayer(entity) && players.getBoolean()) ||
                        (EntityUtils.isHostileMob(entity) && hostileMobs.getBoolean()) ||
                        (EntityUtils.isFriendlyMob(entity) && friendlyMobs.getBoolean())
                ) &&
                isInRange(entPos, selfPos) &&
                isInFOVRange(viewAngles, entPos.subtract(selfPos)) &&
                isVisible(entity);
    }

    /**
     * Check if entity is in attack range
     */
    public boolean isInRange(Vec3d fromPos, Vec3d toPos) {
        double dist = canBowAimbot() ? projectileRange.getDouble() : range.getDouble();
        return dist <= 0 || fromPos.distanceTo(toPos) <= dist;
    }

    public boolean isInFOVRange(Angle selfAngle, Vec3d diffPos) {
        double value = fov.getDouble();
        if (value >= 180) {
            return true;
        } else {
            Angle diff = VectorUtils.vectorAngle(diffPos);
            double pitch = Math.abs(Utils.normalizeAngle(selfAngle.getPitch() - diff.getPitch()));
            double yaw = Math.abs(Utils.normalizeAngle(selfAngle.getYaw() - diff.getYaw()));
            return pitch <= value && yaw <= value;
        }
    }

    /**
     * Finds entity closest to crosshair
     */
    public Entity findTargetEntity(Vec3d selfPos, Vec3d selfLookVec, Angle viewAngles) {
        long start = System.currentTimeMillis();

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

        //System.out.printf("Took %d ms\n", System.currentTimeMillis() - start);
        PlayerUtils.setTargetEntity(target);
        return target;
    }

    @Override
    public void loadConfig(Configuration configuration) {
        addSettings(
                silent = configuration.get(getModName(),
                        "aim_silent",
                        true,
                        "Won't lock onto target"
                ),
                autoAttack = configuration.get(getModName(),
                        "aim_autoattack",
                        true,
                        "Automatically attack"
                ),
                holdTarget = configuration.get(getModName(),
                        "aim_hold_target",
                        false,
                        "Keeps target until it is no longer a valid attack target"
                ),
                projectileAimbot = configuration.get(getModName(),
                        "projectile_aimbot",
                        false,
                        "Aimbot for bows, snowballs, and eggs"
                ),
                projectileTraceCheck = configuration.get(getModName(),
                        "projectile_trace_check",
                        true,
                        "Requires beefy computer, will check if targets can be hit by the bows trajectory"
                ),
                fov = configuration.get(getModName(),
                        "aim_fov",
                        40.D,
                        "Aimbot field of view",
                        0.D,
                        180.D
                ),
                range = configuration.get(getModName(),
                        "aim_range",
                        4.5D,
                        "Attack range"
                ),
                projectileRange = configuration.get(getModName(),
                        "projectile_range",
                        100D,
                        "Attack range for projectiles"
                ),
                cooldownPercent = configuration.get(getModName(),
                        "aim_cooldown_percent",
                        100.D,
                        "What cooldown % to attack again at",
                        0.D,
                        100.D
                ),
                players = configuration.get(getModName(),
                        "tar_players",
                        true,
                        "Attack players"
                ),
                hostileMobs = configuration.get(getModName(),
                        "tar_hostile_mobs",
                        true,
                        "Attack hostile mobs"
                ),
                friendlyMobs = configuration.get(getModName(),
                        "tar_friendly_mobs",
                        true,
                        "Attack friendly mobs"
                )
        );
    }

    @Override
    public void onDisabled() {
        PlayerUtils.setTargetEntity(null);
        PlayerUtils.setActiveFakeAngles(false);
        PlayerUtils.setProjectileTargetAcquired(false);
        PlayerUtils.setFakeViewAngles(null);
    }

    @SubscribeEvent
    public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
        EntityPlayer localPlayer = MC.thePlayer;
        Entity target = PlayerUtils.getTargetEntity();
        Vec3d selfPos = EntityUtils.getEyePos(localPlayer);
        Vec3d selfLookVec = localPlayer.getLookVec();
        Angle viewAngles = VectorUtils.vectorAngle(selfLookVec);
        if(holdTarget.getBoolean()) {
            if(target == null ||
                    !isValidTarget(target, EntityUtils.getOBBCenter(target), selfPos, selfLookVec, viewAngles))
                target = findTargetEntity(selfPos, selfLookVec, viewAngles);
        } else target = findTargetEntity(selfPos, selfLookVec, viewAngles);
        if(target != null) {
            if(!isHoldingProjectileItem()) {
                Angle aim = Utils.getLookAtAngles(target);
                if (!silent.getBoolean())
                    PlayerUtils.setViewAngles(aim);
                if (canAttack(localPlayer, target)) {
                    // attack entity
                    MC.playerController.attackEntity(MC.thePlayer, target);
                    // swing hand
                    localPlayer.swingArm(EnumHand.MAIN_HAND);
                    // for rotation packets
                    if (silent.getBoolean()) {
                        PlayerUtils.setActiveFakeAngles(true);
                        PlayerUtils.setFakeViewAngles(aim);
                        return;
                    }
                }
            } else {
                ItemStack heldItem = localPlayer.getHeldItemMainhand();
                //Vec3d startPos = EntityUtils.getInterpolatedPos(target, 1).addVector(0, target.getEyeHeight(), 0);
                //Vec3d endPos = EntityUtils.getInterpolatedPos(target, 5).addVector(0, target.getEyeHeight() / 2, 0);
                // this will find the angle we need to shoot at
                ProjectileUtils.ProjectileTraceResult result = new ProjectileUtils.ProjectileTraceResult();
                boolean exists = ProjectileUtils.projectileTrajectoryHitsEntity(target, selfPos, getAimPos(target), result);
                if(!exists || result.shootAngle == null) {
                    PlayerUtils.setProjectileTargetAcquired(false);
                } else {
                    // we have a projectile target
                    PlayerUtils.setProjectileTargetAcquired(true);
                    // set view angles
                    PlayerUtils.setFakeViewAngles(result.shootAngle);
                    if (!silent.getBoolean() && Bindings.use.getBinding().isKeyDown()) {
                        PlayerUtils.setViewAngles(result.shootAngle);
                    }
                    // fake angles no active (wont change rotation packets)
                    PlayerUtils.setActiveFakeAngles(false);
                    // bow auto attack will release the use key when
                    // the force is greater than or equal to the max force
                    if(autoAttack.getBoolean() &&
                            Bindings.use.getBinding().isKeyDown() &&
                            ProjectileUtils.getForce(heldItem) >= result.maxForce) {
                        Bindings.use.setPressed(false);
                    }
                    return;
                }
            }
        }
        // disable aiming from last tick
        PlayerUtils.setActiveFakeAngles(false);
        PlayerUtils.setProjectileTargetAcquired(false);
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        /*
        Entity target = PlayerUtils.getTargetEntity();
        if(target != null) {
            Vec3d myPos = getLocalPlayer().getPositionVector();
            Vec3d tarPos = target.getPositionVector();
            Vec3d direction = tarPos.subtract(myPos).normalize();

            //direction = new Vec3d(direction.xCoord, 1, direction.zCoord);
            Vec3d draw = tarPos.add(direction.scale(2));

            RenderUtils.drawBox(draw.subtract(0.05, 0.05, 0.05), draw.addVector(0.05, 0.05, 0.05), Utils.Colors.ORANGE, 2f, true);
        }*/
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPacketSending(PacketEvent.SendEvent.Pre event) {
        if(event.getPacket() instanceof CPacketPlayer) {
            // send fake angles if any rotation updates are sent to the server
            CPacketPlayer packet = (CPacketPlayer)event.getPacket();
            if(packet.rotating &&
                    PlayerUtils.isFakeAnglesActive() &&
                    PlayerUtils.getFakeViewAngles() != null) {
                Angle viewAngles = PlayerUtils.getFakeViewAngles();
                packet.pitch = (float) viewAngles.getPitch();
                packet.yaw = (float) viewAngles.getYaw();
            }
        } else if(event.getPacket() instanceof CPacketPlayerDigging) {
            // called when the bow release packet is sent by the client
            // make sure the packet isn't being called inside this method
            if(((CPacketPlayerDigging) event.getPacket()).getAction().equals(CPacketPlayerDigging.Action.RELEASE_USE_ITEM) &&
                    PlayerUtils.isProjectileTargetAcquired() &&
                    !Utils.OUTGOING_PACKET_IGNORE_LIST.contains(event.getPacket())) {
                // make sure the player is still holding a valid weapon
                EntityPlayer localPlayer = MC.thePlayer;
                ItemStack heldItem = localPlayer.getHeldItemMainhand();
                if(heldItem != null &&
                        ProjectileUtils.isBow(heldItem)) { // bow only
                    // send server our new view angles
                    PlayerUtils.sendRotatePacket(PlayerUtils.getFakeViewAngles());
                    // tell server we let go of bow
                    Packet usePacket = new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN);
                    // add to ignore list
                    Utils.OUTGOING_PACKET_IGNORE_LIST.add(usePacket);
                    getNetworkManager().sendPacket(usePacket);
                    // revert back to the old view angles
                    PlayerUtils.sendRotatePacket(PlayerUtils.getViewAngles());
                    // cancel this event (wont send the packet)
                    event.setCanceled(true);
                }
            }
        } else if(event.getPacket() instanceof CPacketPlayerTryUseItem &&
                PlayerUtils.isProjectileTargetAcquired() &&
                !Utils.OUTGOING_PACKET_IGNORE_LIST.contains(event.getPacket()) &&
                ((CPacketPlayerTryUseItem) event.getPacket()).getHand().equals(EnumHand.MAIN_HAND)) {
            EntityPlayer localPlayer = MC.thePlayer;
            ItemStack heldItem = localPlayer.getHeldItemMainhand();
            if(heldItem != null &&
                    ProjectileUtils.isThrowable(heldItem) &&
                    !ProjectileUtils.isBow(heldItem)) {
                // send server our new view angles
                PlayerUtils.sendRotatePacket(PlayerUtils.getFakeViewAngles());
                // tell server we let go of bow
                Packet usePacket = new CPacketPlayerTryUseItem(((CPacketPlayerTryUseItem) event.getPacket()).getHand());
                // add to ignore list
                Utils.OUTGOING_PACKET_IGNORE_LIST.add(usePacket);
                getNetworkManager().sendPacket(usePacket);
                // revert back to the old view angles
                PlayerUtils.sendRotatePacket(PlayerUtils.getViewAngles());
                // cancel this event (wont send the packet)
                event.setCanceled(true);
            }
        }
    }
}
