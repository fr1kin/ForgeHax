package com.matt.forgehax.mods;

import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.draw.RenderUtils;
import com.matt.forgehax.util.entity.LocalPlayerUtils;
import com.matt.forgehax.util.math.Angle;
import com.matt.forgehax.util.math.ProjectileUtils;
import com.matt.forgehax.util.math.VectorUtils;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.matt.forgehax.Wrapper.getModManager;
import static com.matt.forgehax.Wrapper.getWorld;

@RegisterMod
public class ProjectilesMod extends ToggleMod {
    private static final int TIME = 10;
    private static final double DETAIL = 0.2D;

    public ProjectilesMod() {
        super("Projectiles", false, "Draws projectile path");
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        EntityPlayer localPlayer = MC.player;
        Angle viewAngles = LocalPlayerUtils.getViewAngles();
        Vec3d selfPos = ProjectileUtils.getFiringPos(localPlayer);
        // TODO: Maybe check off hand?
        ItemStack heldItem = localPlayer.getHeldItemMainhand();
        // make sure we are holding a throwable item
        if(heldItem == null || !ProjectileUtils.isThrowable(heldItem))
            return;
        RayTraceResult trace = localPlayer.rayTrace(9999.D, 0.f);
        if(trace == null)
            return;
        double pitch, yaw;
        boolean autoProjectileEnabled;
        try {
            autoProjectileEnabled = getModManager().getMod("AutoProjectile").<Setting>getCommand("enabled").getAsBoolean();
        } catch (Throwable t) {
            autoProjectileEnabled = false;
        }
        if(LocalPlayerUtils.isProjectileTargetAcquired()) {
            pitch = LocalPlayerUtils.getFakeViewAngles().getPitch();
            yaw = LocalPlayerUtils.getFakeViewAngles().getYaw();
        } else if(autoProjectileEnabled) {
            pitch = ProjectileUtils.getBestPitch(heldItem, trace.hitVec);
            yaw = viewAngles.getYaw();
        } else {
            pitch = viewAngles.getPitch();
            yaw = viewAngles.getYaw();
        }
        double force = ProjectileUtils.getForce(heldItem);
        Angle initAngle = new Angle(-pitch, yaw + 90.D, 0.D);

        double fixX = Math.cos(initAngle.getYaw(true) - Math.PI / 2.0) * 0.16;
        double fixY = ProjectileUtils.PROJECTILE_SHOOTPOS_OFFSET;
        double fixZ = Math.sin(initAngle.getYaw(true) - Math.PI / 2.0) * 0.16;
        Vec3d initPos = new Vec3d(-fixX, localPlayer.getEyeHeight() - fixY, -fixZ);

        // convert polar coords to cartesian coords
        Vec3d velocity = initAngle.getCartesianCoords().normalize().scale(force);

        Vec3d acceleration = ProjectileUtils.getGravity(heldItem);
        Vec3d airResistance = ProjectileUtils.getAirResistance(heldItem);

        Vec3d startPos = VectorUtils.copy(initPos);
        Vec3d endPos = VectorUtils.copy(startPos);
        for(int i = 0; i < 100; i++) {
            // add velocity
            startPos = startPos.add(velocity);
            // add air resistance
            velocity = VectorUtils.multiplyBy(velocity, airResistance);
            // add gravity (acceleration)
            velocity = velocity.add(acceleration);

            RenderUtils.drawLine(endPos, startPos, Utils.Colors.WHITE, true, 1.5f);

            RayTraceResult tr = getWorld().rayTraceBlocks(selfPos.add(startPos), selfPos.add(endPos), false, true, false);

            if(tr != null &&
                    !MC.world.getBlockState(tr.getBlockPos()).getBlock().isPassable(getWorld(), tr.getBlockPos())) {
                break;
            }

            endPos = startPos;

            /*
            BlockPos hitPos = new BlockPos(
                    (int)(startPos.xCoord + selfPos.xCoord),
                    (int)(startPos.yCoord + selfPos.yCoord),
                    (int)(startPos.zCoord + selfPos.zCoord)
            );
            // stop if we hit a non-airblock
            if(!MC.theWorld.getBlockState(hitPos).getBlock().isPassable(MC.theWorld, hitPos)) {
                RenderUtils.drawBox(hitPos, hitPos.add(1, 1, 1), Utils.Colors.ORANGE, 1.5f, true);
                break;
            }
            //*/
        }
    }
}
