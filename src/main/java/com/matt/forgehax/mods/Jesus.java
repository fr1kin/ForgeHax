package com.matt.forgehax.mods;

import com.matt.forgehax.asm.events.AddCollisionBoxToListEvent;
import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.matt.forgehax.Helper.*;

/**
 * Created by Babbaj on 8/29/2017.
 */
@RegisterMod
public class Jesus extends ToggleMod {
    private static final AxisAlignedBB WATER_WALK_AA = new AxisAlignedBB(0.D, 0.D, 0.D, 1.D, 0.99D, 1.D);

    public Jesus() { super("Jesus", false, "Walk on water"); }

    @SubscribeEvent
    public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
        if (!getModManager().getMod("Freecam").isEnabled()) {
            if (isInWater(getLocalPlayer()) && !getLocalPlayer().isSneaking()) {
                getLocalPlayer().motionY = 0.1;
                if (getLocalPlayer().getRidingEntity() != null && !(getLocalPlayer().getRidingEntity() instanceof EntityBoat)) {
                    getLocalPlayer().getRidingEntity().motionY = 0.3;
                }
            }
        }
    }

    @SubscribeEvent
    public void onAddCollisionBox(AddCollisionBoxToListEvent event) {
        if (getLocalPlayer() != null
                && EntityUtils.isLocalPlayer(event.getEntity())
                && event.getBlock() instanceof BlockLiquid
                && !EntityUtils.isDrivenByPlayer(event.getEntity())
                && !getLocalPlayer().isSneaking()
                && getLocalPlayer().fallDistance < 3
                && !isInWater(getLocalPlayer())) {
            AxisAlignedBB axisalignedbb = WATER_WALK_AA.offset(event.getPos());
            if (event.getEntityBox().intersects(axisalignedbb)) event.getCollidingBoxes().add(axisalignedbb);
            // cancel event, which will stop it from calling the original code
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPacketSending(PacketEvent.Outgoing.Pre event) {
        if (event.getPacket() instanceof CPacketPlayer) {
            if (isAboveWater(getLocalPlayer()) && !isInWater(getLocalPlayer()) && !isAboveLand(getLocalPlayer())) {
                int ticks = getLocalPlayer().ticksExisted % 2;
                double y = FastReflection.Fields.CPacketPlayer_Y.get(event.getPacket());
                if (ticks == 0) FastReflection.Fields.CPacketPlayer_Y.set(event.getPacket(), y + 0.02D );
            }
        }

    }

    @SuppressWarnings("deprecation")
    private static boolean isAboveLand(Entity entity){
        if(entity == null) return false;

        double y = entity.posY - 0.01;

        for(int x = MathHelper.floor(entity.posX); x < MathHelper.ceil(entity.posX); x++)
            for (int z = MathHelper.floor(entity.posZ); z < MathHelper.ceil(entity.posZ); z++) {
                BlockPos pos = new BlockPos(x, MathHelper.floor(y), z);

                if (getWorld().getBlockState(pos).getBlock().isFullBlock(getWorld().getBlockState(pos))) return true;
            }

        return false;
    }

    private static boolean isAboveWater(Entity entity){
        double y = entity.posY - 0.03;

        for(int x = MathHelper.floor(entity.posX); x < MathHelper.ceil(entity.posX); x++)
            for (int z = MathHelper.floor(entity.posZ); z < MathHelper.ceil(entity.posZ); z++) {
                BlockPos pos = new BlockPos(x, MathHelper.floor(y), z);

                if (getWorld().getBlockState(pos).getBlock() instanceof BlockLiquid) return true;
            }

        return false;
    }

    private static boolean isInWater(Entity entity) {
        if(entity == null) return false;

        double y = entity.posY + 0.01;

        for(int x = MathHelper.floor(entity.posX); x < MathHelper.ceil(entity.posX); x++)
            for (int z = MathHelper.floor(entity.posZ); z < MathHelper.ceil(entity.posZ); z++) {
                BlockPos pos = new BlockPos(x, (int) y, z);

                if (getWorld().getBlockState(pos).getBlock() instanceof BlockLiquid) return true;
            }

        return false;
    }

}