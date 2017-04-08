package com.matt.forgehax.asm2;

import com.fr1kin.asmhelper.Stage;
import com.matt.forgehax.asm.events.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;

/**
 * Created on 1/17/2017 by fr1kin
 */
public class MethodHooks {
    //
    // Entity
    //
    public static boolean onApplyCollisionMotion(Entity entity, Entity collidedWithEntity, double x, double z) {
        return MinecraftForge.EVENT_BUS.post(new ApplyCollisionMotionEvent(entity, collidedWithEntity, x, 0.D, z));
    }
    public static boolean onDoBlockCollisions(Entity entity, BlockPos pos, IBlockState state) {
        return MinecraftForge.EVENT_BUS.post(new DoBlockCollisionsEvent(entity, pos, state));
    }

    //
    // EntityRenderer
    //
    public static boolean onHurtcamEffect(int stage, EntityRenderer entityRenderer, float partialTicks) {
        switch (stage) {
            case Stage.PRE:
                return MinecraftForge.EVENT_BUS.post(new HurtCamEffectEvent(partialTicks));
            default:
                return false;
        }
    }

    //
    // NetworkManager
    //
    public static boolean onPacketOutgoing(int stage, NetworkManager networkManager, Packet packet) {
        switch (stage) {
            case Stage.PRE:
                return MinecraftForge.EVENT_BUS.post(new PacketEvent.Outgoing.Pre(packet));
            case Stage.POST:
            default:
                MinecraftForge.EVENT_BUS.post(new PacketEvent.Outgoing.Post(packet));
                return false;
        }
    }
    public static boolean onPacketIncoming(int stage, NetworkManager networkManager, Packet packet) {
        switch (stage) {
            case Stage.PRE:
                return MinecraftForge.EVENT_BUS.post(new PacketEvent.Incoming.Pre(packet));
            case Stage.POST:
            default:
                MinecraftForge.EVENT_BUS.post(new PacketEvent.Incoming.Post(packet));
                return false;
        }
    }

    //
    // RenderGlobal
    //
    public static boolean onRenderLayer(int stage, RenderGlobal renderGlobal, BlockRenderLayer blockRenderLayer, double partialTicks, int pass, Entity entityIn) {
        switch (stage) {
            case Stage.PRE:
                return MinecraftForge.EVENT_BUS.post(new RenderBlockLayerEvent.Pre(blockRenderLayer, partialTicks));
            case Stage.POST:
            default:
                MinecraftForge.EVENT_BUS.post(new RenderBlockLayerEvent.Post(blockRenderLayer, partialTicks));
                return false;
        }
    }
}
