package com.matt.forgehax.mods;

import com.google.common.collect.Lists;
import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.mods.managers.PositionRotationManager;
import com.matt.forgehax.mods.services.TickRateService;
import com.matt.forgehax.util.PacketHelper;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.common.PriorityEnum;
import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.entity.LocalPlayerUtils;
import com.matt.forgehax.util.entity.PlayerUtils;
import com.matt.forgehax.util.key.Bindings;
import com.matt.forgehax.util.math.Angle;
import com.matt.forgehax.util.math.ProjectileUtils;
import com.matt.forgehax.util.math.VectorUtils;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
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
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collections;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getNetworkManager;

@RegisterMod
public class AimbotMod extends ToggleMod implements PositionRotationManager.MovementUpdateListener {
    public final Setting<Boolean> silent = getCommandStub().builders().<Boolean>newSettingBuilder()
            .name("silent")
            .description("Won't look at target when aiming")
            .defaultTo(true)
            .build();

    public final Setting<Boolean> auto_attack = getCommandStub().builders().<Boolean>newSettingBuilder()
            .name("autoattack")
            .description("Automatically attack when target found")
            .defaultTo(true)
            .build();

    public final Setting<Boolean> projectile_auto_attack = getCommandStub().builders().<Boolean>newSettingBuilder()
            .name("projectile_auto_attack")
            .description("Automatically attack when target found for projectile weapons")
            .defaultTo(true)
            .build();

    public final Setting<Boolean> hold_target = getCommandStub().builders().<Boolean>newSettingBuilder()
            .name("hold_target")
            .description("Keep first caught target until it becomes no longer valid")
            .defaultTo(false)
            .build();

    public final Setting<Boolean> vis_check = getCommandStub().builders().<Boolean>newSettingBuilder()
            .name("vis_check")
            .description("Check if the target is visible before acquiring")
            .defaultTo(false)
            .build();

    public final Setting<Boolean> projectile_aimbot = getCommandStub().builders().<Boolean>newSettingBuilder()
            .name("projectile_aimbot")
            .description("Projectile aimbot")
            .defaultTo(true)
            .build();

    public final Setting<Boolean> projectile_trace_check = getCommandStub().builders().<Boolean>newSettingBuilder()
            .name("projectile_trace_check")
            .description("Check the trace of each target if holding a weapon that fires a projectile")
            .defaultTo(true)
            .build();

    public final Setting<Boolean> target_players = getCommandStub().builders().<Boolean>newSettingBuilder()
            .name("target_players")
            .description("Target players")
            .defaultTo(true)
            .build();

    public final Setting<Boolean> target_mobs_hostile = getCommandStub().builders().<Boolean>newSettingBuilder()
            .name("target_mobs_hostile")
            .description("Target hostile mobs")
            .defaultTo(true)
            .build();

    public final Setting<Boolean> target_mobs_friendly = getCommandStub().builders().<Boolean>newSettingBuilder()
            .name("target_mobs_friendly")
            .description("Target friendly mobs")
            .defaultTo(false)
            .build();

    public final Setting<Boolean> lag_compensation = getCommandStub().builders().<Boolean>newSettingBuilder()
            .name("lag_compensation")
            .description("Compensate for server lag")
            .defaultTo(true)
            .build();

    public final Setting<Integer> fov = getCommandStub().builders().<Integer>newSettingBuilder()
            .name("fov")
            .description("Aimbot field of view")
            .defaultTo(180)
            .min(0)
            .max(180)
            .build();

    public final Setting<Double> range = getCommandStub().builders().<Double>newSettingBuilder()
            .name("range")
            .description("Aimbot range")
            .defaultTo(4.5D)
            .build();

    public final Setting<Double> projectile_range = getCommandStub().builders().<Double>newSettingBuilder()
            .name("projectile_range")
            .description("Projectile aimbot range")
            .defaultTo(100D)
            .build();

    public final Setting<Double> cooldown_percent = getCommandStub().builders().<Double>newSettingBuilder()
            .name("cooldown_percent")
            .description("Minimum cooldown percent for next strike")
            .defaultTo(100D)
            .build();

    public AimbotMod() {
        super(Category.COMBAT, "Aimbot", false, "Automatically attack entities and players");
    }

    private double getLagComp() {
        if(lag_compensation.get()) {
            return -(20.D - TickRateService.getTickData().getPoint().getAverage());
        } else return 0.D;
    }

    public boolean canAttack(EntityPlayer localPlayer, Entity target) {
        return localPlayer.getCooledAttackStrength((float)getLagComp()) >= (cooldown_percent.get() / 100.D) &&
                (auto_attack.get() || Bindings.attack.getBinding().isKeyDown()); // need to work on this
    }

    public boolean isHoldingProjectileItem() {
        return ProjectileUtils.isThrowable(MC.player.getHeldItemMainhand());
    }

    public boolean isProjectileAimbotActivated() {
        return projectile_aimbot.get() &&
                isHoldingProjectileItem();
    }

    public boolean isVisible(Entity target) {
        if(isProjectileAimbotActivated() && projectile_trace_check.get()) {
            return ProjectileUtils.projectileTrajectoryHitsEntity(target, EntityUtils.getEyePos(getLocalPlayer()), getAimPos(target), null);
        } else return !vis_check.get() || getLocalPlayer().canEntityBeSeen(target);
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
                !entity.equals(MC.player) &&
                EntityUtils.isValidEntity(entity) && (
                (EntityUtils.isPlayer(entity) && target_players.get() && !PlayerUtils.isFriend((EntityPlayer)entity)) ||
                        (EntityUtils.isHostileMob(entity) && target_mobs_hostile.get()) ||
                        (EntityUtils.isFriendlyMob(entity) && target_mobs_friendly.get())
                ) &&
                isInRange(entPos, selfPos) &&
                isInFOVRange(viewAngles, entPos.subtract(selfPos)) &&
                isVisible(entity);
    }

    /**
     * Check if entity is in attack range
     */
    public boolean isInRange(Vec3d fromPos, Vec3d toPos) {
        double dist = isProjectileAimbotActivated() ? projectile_range.get() : range.get();
        return dist <= 0 || fromPos.distanceTo(toPos) <= dist;
    }

    public boolean isInFOVRange(Angle selfAngle, Vec3d diffPos) {
        double value = fov.get();
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
        final World world = Minecraft.getMinecraft().world;
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
        LocalPlayerUtils.setTargetEntity(target);
        return target;
    }

    @Override
    protected void onEnabled() {
        PositionRotationManager.getManager().register(this, PriorityEnum.HIGH);
    }

    @Override
    public void onDisabled() {
        PositionRotationManager.getManager().unregister(this);

        LocalPlayerUtils.setTargetEntity(null);
        LocalPlayerUtils.setActiveFakeAngles(false);
        LocalPlayerUtils.setProjectileTargetAcquired(false);
        LocalPlayerUtils.setFakeViewAngles(null);
    }

    /*
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPacketSending(PacketEvent.Outgoing.Pre event) {
        if(event.getPacket() instanceof CPacketPlayerDigging) {
            // called when the bow release packet is sent by the client
            // make sure the packet isn't being called inside this method
            if(((CPacketPlayerDigging) event.getPacket()).getAction().equals(CPacketPlayerDigging.Action.RELEASE_USE_ITEM) &&
                    LocalPlayerUtils.isProjectileTargetAcquired() &&
                    !PacketHelper.isIgnored(event.getPacket())) {
                // make sure the player is still holding a valid weapon
                EntityPlayer localPlayer = MC.player;
                ItemStack heldItem = localPlayer.getHeldItemMainhand();
                if(heldItem != null &&
                        ProjectileUtils.isBow(heldItem)) { // bow only
                    // send server our new view angles
                    LocalPlayerUtils.sendRotatePacket(LocalPlayerUtils.getFakeViewAngles());
                    // tell server we let go of bow
                    Packet usePacket = new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN);
                    // add to ignore list
                    PacketHelper.ignore(usePacket);
                    getNetworkManager().sendPacket(usePacket);
                    // revert back to the old view angles
                    LocalPlayerUtils.sendRotatePacket(LocalPlayerUtils.getViewAngles());
                    // cancel this event (wont send the packet)
                    event.setCanceled(true);
                }
            }
        } else if(event.getPacket() instanceof CPacketPlayerTryUseItem &&
                LocalPlayerUtils.isProjectileTargetAcquired() &&
                !PacketHelper.isIgnored(event.getPacket()) &&
                ((CPacketPlayerTryUseItem) event.getPacket()).getHand().equals(EnumHand.MAIN_HAND)) {
            EntityPlayer localPlayer = MC.player;
            ItemStack heldItem = localPlayer.getHeldItemMainhand();
            if(heldItem != null &&
                    ProjectileUtils.isThrowable(heldItem) &&
                    !ProjectileUtils.isBow(heldItem)) {
                // send server our new view angles
                LocalPlayerUtils.sendRotatePacket(LocalPlayerUtils.getFakeViewAngles());
                // tell server we let go of bow
                Packet usePacket = new CPacketPlayerTryUseItem(((CPacketPlayerTryUseItem) event.getPacket()).getHand());
                // add to ignore list
                PacketHelper.ignore(usePacket);
                getNetworkManager().sendPacket(usePacket);
                // revert back to the old view angles
                LocalPlayerUtils.sendRotatePacket(LocalPlayerUtils.getViewAngles());
                // cancel this event (wont send the packet)
                event.setCanceled(true);
            }
        }
    }*/

    @Override
    public void onLocalPlayerMovementUpdate(PositionRotationManager.RotationState state) {
        EntityPlayer localPlayer = MC.player;
        Entity target = LocalPlayerUtils.getTargetEntity();
        // local player eye pos
        Vec3d selfPos = EntityUtils.getEyePos(localPlayer);
        // local player look vec
        Vec3d selfLookVec = localPlayer.getLookVec();
        // local player view angles
        Angle viewAngles = VectorUtils.vectorAngle(selfLookVec);
        if(hold_target.get()) {
            if(target == null ||
                    !isValidTarget(target, EntityUtils.getOBBCenter(target), selfPos, selfLookVec, viewAngles)) {
                target = findTargetEntity(selfPos, selfLookVec, viewAngles);
            }
        } else {
            target = findTargetEntity(selfPos, selfLookVec, viewAngles);
        }
        if(target != null) {
            if(!isHoldingProjectileItem()) {
                Angle aim = Utils.getLookAtAngles(target);
                state.setViewAngles((float)aim.getPitch(), (float)aim.getYaw(), silent.get());

                if (canAttack(localPlayer, target)) {
                    final Entity t = target;
                    state.processAfter(() -> {
                        // attack entity
                        MC.playerController.attackEntity(MC.player, t);
                        // swing hand
                        localPlayer.swingArm(EnumHand.MAIN_HAND);
                    });
                }
            } else {
                ItemStack holding = localPlayer.getHeldItemMainhand();

                // this will find the angle we need to shoot at
                ProjectileUtils.ProjectileTraceResult result = new ProjectileUtils.ProjectileTraceResult();
                boolean exists = ProjectileUtils.projectileTrajectoryHitsEntity(target, selfPos, getAimPos(target), result);
                if(!exists || result.shootAngle == null) {
                    LocalPlayerUtils.setProjectileTargetAcquired(false);
                } else {
                    // we have a projectile target
                    LocalPlayerUtils.setProjectileTargetAcquired(true);

                    // set view angles
                    state.setViewAngles((float)result.shootAngle.getPitch(), (float)result.shootAngle.getYaw(), silent.get());

                    // bow auto attack will release the use key when
                    // the force is greater than or equal to the max force
                    if(projectile_auto_attack.get() &&
                            Bindings.use.getBinding().isKeyDown() &&
                            ProjectileUtils.getForce(holding) >= result.maxForce) {
                        Bindings.use.setPressed(false);
                    }
                    return;
                }
            }
        }
        LocalPlayerUtils.setProjectileTargetAcquired(false);
    }
}
