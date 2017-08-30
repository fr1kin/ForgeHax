package com.matt.forgehax.mods;

import com.matt.forgehax.asm.ForgeHaxHooks;
import com.matt.forgehax.asm.events.AddCollisionBoxToListEvent;
import com.matt.forgehax.asm.events.LocalPlayerUpdateMovementEvent;
import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.entity.LocalPlayerUtils;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.matt.forgehax.Helper.getModManager;

/**
 * Created by Babbaj on 8/29/2017.
 */
@RegisterMod
public class Jesus extends ToggleMod {
    public Jesus() { super("Jesus", false, "Walk on water"); }


    @SubscribeEvent
    public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
        if (!getModManager().getMod("Freecam").<Setting>getCommand("enabled").getAsBoolean()) {
            if (isInWater(MC.player) && !MC.player.isSneaking()) {
                MC.player.motionY = 0.1;
                if (MC.player.getRidingEntity() != null && !(MC.player.getRidingEntity() instanceof EntityBoat)) {
                    MC.player.getRidingEntity().motionY = 0.3;
                }
            }
        }
    }

    @SubscribeEvent
    public void onAddCollisionBox(AddCollisionBoxToListEvent event) {
        if (MC.player == null) return;

        AxisAlignedBB bb = new AxisAlignedBB(0, 0, 0, 1, 0.99, 1);

        if (!(event.getBlock() instanceof BlockLiquid) || !(EntityUtils.isDrivenByPlayer(event.getEntity()) || EntityUtils.isPlayer(event.getEntity()))) {
            bb = null;
        }

        if (isInWater(MC.player) || MC.player.isSneaking() || MC.player.fallDistance > 3) {
            bb = null;
        }

        ForgeHaxHooks.blockBoxOverride = bb;
    }
    @Override
    public void onDisabled() {
        ForgeHaxHooks.blockBoxOverride = null;
    }

    @SubscribeEvent
    public void onPacketSending(PacketEvent.Outgoing.Pre event) {
        if (event.getPacket() instanceof CPacketPlayer) {
            if (isAboveWater(MC.player) && !isInWater(MC.player) && !isAboveLand(MC.player)) {
                int ticks = MC.player.ticksExisted % 2;
                double Y = FastReflection.Fields.CPacketPlayer_Y.get(event.getPacket());

                if (ticks == 0) FastReflection.Fields.CPacketPlayer_Y.set(event.getPacket(), Y + 0.02 );

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

                if (MC.world.getBlockState(pos).getBlock().isFullBlock(MC.world.getBlockState(pos))) return true;
            }

        return false;
    }

    private static boolean isAboveWater(Entity entity){
        double y = entity.posY - 0.03;

        for(int x = MathHelper.floor(entity.posX); x < MathHelper.ceil(entity.posX); x++)
            for (int z = MathHelper.floor(entity.posZ); z < MathHelper.ceil(entity.posZ); z++) {
                BlockPos pos = new BlockPos(x, MathHelper.floor(y), z);

                if (MC.world.getBlockState(pos).getBlock() instanceof BlockLiquid) return true;
            }

        return false;
    }

    private static boolean isInWater(Entity entity) {
        if(entity == null) return false;

        double y = entity.posY + 0.01;

        for(int x = MathHelper.floor(entity.posX); x < MathHelper.ceil(entity.posX); x++)
            for (int z = MathHelper.floor(entity.posZ); z < MathHelper.ceil(entity.posZ); z++) {
                BlockPos pos = new BlockPos(x, (int) y, z);

                if (MC.world.getBlockState(pos).getBlock() instanceof BlockLiquid) return true;
            }

        return false;
    }

}